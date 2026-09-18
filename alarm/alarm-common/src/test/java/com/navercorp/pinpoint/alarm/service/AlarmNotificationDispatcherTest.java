/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationService;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationChannelConfigParser;
import com.navercorp.pinpoint.alarm.sender.AlarmSendException;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutboxCounts;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutboxStatus;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.lang.reflect.Proxy;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmNotificationDispatcherTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private InMemoryOutboxDao outboxDao;
    private InMemoryHistoryDao historyDao;
    private InMemoryStateDao stateDao;
    private RecordingNotificationService notificationService;
    private AlarmNotificationOutboxClaimService claimService;
    private AlarmNotificationDispatcher dispatcher;
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-07-16T00:00:00Z"));
        outboxDao = new InMemoryOutboxDao();
        historyDao = new InMemoryHistoryDao();
        stateDao = new InMemoryStateDao();
        notificationService = new RecordingNotificationService(objectMapper);
        NoOpTransactionManager transactionManager = new NoOpTransactionManager();
        claimService = new AlarmNotificationOutboxClaimService(outboxDao, transactionManager);
        AlarmNotificationResultService resultService = new AlarmNotificationResultService(
                outboxDao, stateDao, historyDao, transactionManager, objectMapper);
        dispatcher = new AlarmNotificationDispatcher(
                claimService, notificationService, resultService,
                10, 5, Duration.ofMinutes(1), clock);
    }

    // attempt_count is TINYINT UNSIGNED, and a claim past the budget increments it once more
    // before the row is marked dead, so the ceiling has to stop one short of the column.
    @Test
    void maxAttemptsAboveTheColumnRangeIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new AlarmNotificationDispatcher(claimService, notificationService,
                        new AlarmNotificationResultService(outboxDao, stateDao, historyDao,
                                new NoOpTransactionManager(), objectMapper),
                        10, 255, Duration.ofMinutes(1), clock));
        assertTrue(e.getMessage().contains("254"), e.getMessage());
    }

    // A worker that dies between claiming and recording leaves the row PROCESSING with its
    // attempt already counted. Lease recovery must not keep sending it: the send would repeat
    // forever and the counter would climb until the increment overflowed the column.
    @Test
    void leaseRecoveryOfAnExhaustedDeliveryIsDeadLetteredWithoutSending() {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);
        delivery.setStatus(AlarmNotificationOutboxStatus.PROCESSING);
        delivery.setAttemptCount(5);

        assertEquals(1, dispatcher.dispatch());

        assertEquals(AlarmNotificationOutboxStatus.DEAD, delivery.getStatus());
        assertEquals(List.of(), notificationService.deliveryIds);
    }

    /**
     * Deliveries are sent one at a time and a provider can take as long as it likes, so a full
     * batch can outlast the lease it was claimed under. Anything still waiting past that point
     * may already have been claimed by another dispatcher, and sending it here would deliver
     * the same notification twice -- so the run stops and leaves the rest to be re-claimed.
     */
    @Test
    void dispatchStopsAtTheLeaseAndLeavesTheRestClaimable() {
        AlarmNotificationOutbox first = seedDelivery(1L, 100L, 10L);
        AlarmNotificationOutbox second = seedDelivery(2L, 100L, 20L);
        AlarmNotificationOutbox third = seedDelivery(3L, 100L, 30L);
        // Lease is one minute; each send burns forty seconds of it.
        notificationService.clock = clock;
        notificationService.sendDuration = Duration.ofSeconds(40);

        assertEquals(2, dispatcher.dispatch());

        assertEquals(List.of(1L, 2L), notificationService.deliveryIds);
        assertEquals(AlarmNotificationOutboxStatus.SENT, first.getStatus());
        // Left as claimed, so the next run finds it once the lease lapses.
        assertEquals(AlarmNotificationOutboxStatus.SENT, second.getStatus());
        assertEquals(AlarmNotificationOutboxStatus.PROCESSING, third.getStatus());
    }

    @Test
    void successMarksSentUpdatesActualNotificationTimeAndHistory() throws Exception {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);

        assertEquals(1, dispatcher.dispatch());

        assertEquals(AlarmNotificationOutboxStatus.SENT, delivery.getStatus());
        assertNull(delivery.getAvailableAt());
        assertEquals(LocalDateTime.now(clock), stateDao.state.getLastNotifiedAt());
        JsonNode context = objectMapper.readTree(historyDao.history.getContext());
        assertEquals(1, context.path("notification").path("sent").asInt());
        assertEquals(0, context.path("notification").path("pending").asInt());
        assertEquals(1, outboxDao.selectCountsCalls);
        assertEquals(1, historyDao.selectForUpdateCalls);
        assertEquals(1, historyDao.updateContextCalls);
    }

    @Test
    void transientFailureRetriesOnlyFailedChannelWithStableDeliveryId() throws Exception {
        AlarmNotificationOutbox first = seedDelivery(1L, 100L, 10L);
        AlarmNotificationOutbox second = seedDelivery(2L, 100L, 20L);
        notificationService.transientFailures.put(second.getId(), 1);

        dispatcher.dispatch();

        assertEquals(AlarmNotificationOutboxStatus.SENT, first.getStatus());
        assertEquals(AlarmNotificationOutboxStatus.RETRY, second.getStatus());
        assertEquals(LocalDateTime.now(clock).plusMinutes(1), second.getAvailableAt());
        assertEquals(List.of(1L, 2L), notificationService.deliveryIds);
        JsonNode firstAttemptContext = objectMapper.readTree(historyDao.history.getContext());
        assertEquals(1, firstAttemptContext.path("notification").path("sent").asInt());
        assertEquals(1, firstAttemptContext.path("notification").path("pending").asInt());
        assertEquals(1, outboxDao.selectCountsCalls);
        assertEquals(1, historyDao.selectForUpdateCalls);
        assertEquals(1, historyDao.updateContextCalls);

        clock.advance(Duration.ofMinutes(1));
        dispatcher.dispatch();

        assertEquals(AlarmNotificationOutboxStatus.SENT, second.getStatus());
        assertEquals(List.of(1L, 2L, 2L), notificationService.deliveryIds);
        JsonNode secondAttemptContext = objectMapper.readTree(historyDao.history.getContext());
        assertEquals(2, secondAttemptContext.path("notification").path("sent").asInt());
        assertEquals(0, secondAttemptContext.path("notification").path("pending").asInt());
        assertEquals(2, outboxDao.selectCountsCalls);
        assertEquals(2, historyDao.selectForUpdateCalls);
        assertEquals(2, historyDao.updateContextCalls);
    }

    @Test
    void fifthFailedAttemptMovesDeliveryToDead() throws Exception {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);
        delivery.setAttemptCount(4);
        notificationService.transientFailures.put(delivery.getId(), 1);

        dispatcher.dispatch();

        assertEquals(5, delivery.getAttemptCount());
        assertEquals(AlarmNotificationOutboxStatus.DEAD, delivery.getStatus());
        JsonNode context = objectMapper.readTree(historyDao.history.getContext());
        assertEquals(1, context.path("notification").path("failed").asInt());
        assertEquals(0, context.path("notification").path("pending").asInt());
        assertEquals(1, outboxDao.selectCountsCalls);
        assertEquals(1, historyDao.selectForUpdateCalls);
        assertEquals(1, historyDao.updateContextCalls);
    }

    @Test
    void retryBackoffIsOneFiveFifteenAndSixtyMinutes() {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);
        notificationService.transientFailures.put(delivery.getId(), 4);

        for (Duration expectedDelay : List.of(
                Duration.ofMinutes(1), Duration.ofMinutes(5),
                Duration.ofMinutes(15), Duration.ofHours(1))) {
            LocalDateTime attemptTime = LocalDateTime.now(clock);

            dispatcher.dispatch();

            assertEquals(AlarmNotificationOutboxStatus.RETRY, delivery.getStatus());
            assertEquals(attemptTime.plus(expectedDelay), delivery.getAvailableAt());
            clock.advance(expectedDelay);
        }

        assertEquals(4, delivery.getAttemptCount());
        assertEquals(0, outboxDao.selectCountsCalls);
        assertEquals(0, historyDao.selectForUpdateCalls);
        assertEquals(0, historyDao.updateContextCalls);
    }

    @Test
    void permanentClientErrorMovesDirectlyToDead() {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);
        HttpClientErrorException badRequest = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "bad request", HttpHeaders.EMPTY,
                new byte[0], StandardCharsets.UTF_8);
        notificationService.permanentFailures.put(delivery.getId(), badRequest);

        dispatcher.dispatch();

        assertEquals(1, delivery.getAttemptCount());
        assertEquals(AlarmNotificationOutboxStatus.DEAD, delivery.getStatus());
    }

    /**
     * A stored URL that no longer validates cannot start working, so it must not consume retries.
     */
    @Test
    void invalidWebhookUrlMovesDirectlyToDead() {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);
        notificationService.permanentFailures.put(delivery.getId(),
                new IllegalArgumentException("Malformed webhook URL"));

        dispatcher.dispatch();

        assertEquals(1, delivery.getAttemptCount());
        assertEquals(AlarmNotificationOutboxStatus.DEAD, delivery.getStatus());
    }

    /**
     * A host rejected by the webhook host policy resolves the same way on every attempt. The resolver
     * reports it as UnknownHostException caused by IllegalArgumentException, which is what separates
     * it from a DNS outage.
     */
    @Test
    void webhookHostRejectedByPolicyMovesDirectlyToDead() {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);
        UnknownHostException blockedByPolicy = new UnknownHostException(
                "Webhook host resolves to a non-public address. host=internal.example.com");
        blockedByPolicy.initCause(new IllegalArgumentException("resolves to a private address"));
        notificationService.permanentFailures.put(delivery.getId(),
                new ResourceAccessException("I/O error", new java.io.IOException(blockedByPolicy)));

        dispatcher.dispatch();

        assertEquals(1, delivery.getAttemptCount());
        assertEquals(AlarmNotificationOutboxStatus.DEAD, delivery.getStatus());
    }

    /**
     * A DNS outage carries no IllegalArgumentException, so it stays retryable.
     */
    @Test
    void dnsOutageIsStillRetried() {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);
        notificationService.permanentFailures.put(delivery.getId(),
                new ResourceAccessException("I/O error",
                        new java.io.IOException(new UnknownHostException("dns.example.com"))));

        dispatcher.dispatch();

        assertEquals(1, delivery.getAttemptCount());
        assertEquals(AlarmNotificationOutboxStatus.RETRY, delivery.getStatus());
    }

    @Test
    void expiredProcessingLeaseIsClaimedAgain() {
        AlarmNotificationOutbox delivery = seedDelivery(1L, 100L, 10L);
        delivery.setStatus(AlarmNotificationOutboxStatus.PROCESSING);
        delivery.setClaimToken("dead-worker");
        delivery.setAvailableAt(LocalDateTime.now(clock).minusSeconds(1));
        delivery.setAttemptCount(1);

        dispatcher.dispatch();

        assertEquals(2, delivery.getAttemptCount());
        assertEquals(AlarmNotificationOutboxStatus.SENT, delivery.getStatus());
    }

    @Test
    void activeLeaseCannotBeClaimedBySecondDispatcher() {
        seedDelivery(1L, 100L, 10L);

        List<AlarmNotificationOutbox> first = claimService.claim(
                10, LocalDateTime.now(clock), Duration.ofMinutes(1));
        List<AlarmNotificationOutbox> second = claimService.claim(
                10, LocalDateTime.now(clock), Duration.ofMinutes(1));

        assertEquals(1, first.size());
        assertTrue(second.isEmpty());
        assertFalse(first.get(0).getClaimToken().isBlank());
    }

    private AlarmNotificationOutbox seedDelivery(Long id, Long historyId, Long channelId) {
        if (historyDao.history == null) {
            AlarmHistoryV2 history = new AlarmHistoryV2();
            history.setId(historyId);
            history.setRuleId(7L);
            history.setContext("{\"notification\":{\"sent\":0,\"failed\":0,\"pending\":2}}");
            historyDao.history = history;
        }
        if (stateDao.state == null) {
            stateDao.state = new AlarmState(7L);
        }
        AlarmNotificationOutbox delivery = new AlarmNotificationOutbox();
        delivery.setId(id);
        delivery.setHistoryId(historyId);
        delivery.setChannelId(channelId);
        delivery.setMethodType(AlarmMethodType.EMAIL);
        delivery.setPayload("{}");
        delivery.setStatus(AlarmNotificationOutboxStatus.PENDING);
        delivery.setAvailableAt(LocalDateTime.now(clock));
        outboxDao.deliveries.add(delivery);
        return delivery;
    }

    @SuppressWarnings("unchecked")
    private static <T> T noop(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> List.class.isAssignableFrom(method.getReturnType()) ? List.of() : null);
    }

    private static class RecordingNotificationService extends AlarmNotificationService {
        private final List<Long> deliveryIds = new ArrayList<>();
        private final Map<Long, Integer> transientFailures = new HashMap<>();
        private final Map<Long, RuntimeException> permanentFailures = new HashMap<>();
        private Duration sendDuration = Duration.ZERO;
        private MutableClock clock;

        private RecordingNotificationService(ObjectMapper objectMapper) {
            super(noop(AlarmChannelBindingDao.class), noop(AlarmNotificationChannelDao.class),
                    List.of(), objectMapper, new AlarmNotificationChannelConfigParser(objectMapper));
        }

        @Override
        public void deliver(AlarmNotificationOutbox delivery) {
            deliveryIds.add(delivery.getId());
            if (clock != null && !sendDuration.isZero()) {
                clock.advance(sendDuration);
            }
            RuntimeException permanent = permanentFailures.get(delivery.getId());
            if (permanent != null) {
                throw new AlarmSendException("permanent", permanent);
            }
            int remaining = transientFailures.getOrDefault(delivery.getId(), 0);
            if (remaining > 0) {
                transientFailures.put(delivery.getId(), remaining - 1);
                throw new AlarmSendException("temporary", new ResourceAccessException("timeout"));
            }
        }
    }

    private static class InMemoryOutboxDao implements AlarmNotificationOutboxDao {
        private final List<AlarmNotificationOutbox> deliveries = new ArrayList<>();
        private int selectCountsCalls;

        @Override public void insert(AlarmNotificationOutbox delivery) { deliveries.add(delivery); }

        @Override
        public synchronized List<Long> selectClaimCandidateIds(LocalDateTime now, int limit) {
            return deliveries.stream()
                    .filter(delivery -> isAvailable(delivery, now))
                    .sorted((left, right) -> Long.compare(left.getId(), right.getId()))
                    .limit(limit)
                    .map(AlarmNotificationOutbox::getId)
                    .toList();
        }

        @Override
        public synchronized int claimAvailable(String token,
                                               LocalDateTime now,
                                               LocalDateTime availableAt,
                                               List<Long> ids) {
            List<AlarmNotificationOutbox> available = deliveries.stream()
                    .filter(delivery -> ids.contains(delivery.getId()))
                    .filter(delivery -> isAvailable(delivery, now))
                    .sorted((left, right) -> Long.compare(left.getId(), right.getId()))
                    .toList();
            available.forEach(delivery -> {
                delivery.setStatus(AlarmNotificationOutboxStatus.PROCESSING);
                delivery.setClaimToken(token);
                delivery.setAvailableAt(availableAt);
                delivery.setAttemptCount(delivery.getAttemptCount() + 1);
            });
            return available.size();
        }

        private boolean isAvailable(AlarmNotificationOutbox delivery, LocalDateTime now) {
            if (delivery.getStatus() == AlarmNotificationOutboxStatus.PENDING
                    || delivery.getStatus() == AlarmNotificationOutboxStatus.RETRY) {
                return !delivery.getAvailableAt().isAfter(now);
            }
            return delivery.getStatus() == AlarmNotificationOutboxStatus.PROCESSING
                    && delivery.getAvailableAt() != null
                    && !delivery.getAvailableAt().isAfter(now);
        }

        @Override
        public synchronized List<AlarmNotificationOutbox> selectByClaimToken(String token) {
            return deliveries.stream()
                    .filter(delivery -> delivery.getStatus() == AlarmNotificationOutboxStatus.PROCESSING
                            && token.equals(delivery.getClaimToken()))
                    .toList();
        }

        @Override public synchronized int markSent(Long id, String token) {
            AlarmNotificationOutbox delivery = claimed(id, token);
            if (delivery == null) return 0;
            delivery.setStatus(AlarmNotificationOutboxStatus.SENT);
            delivery.setClaimToken(null);
            delivery.setAvailableAt(null);
            return 1;
        }

        @Override public synchronized int markRetry(Long id, String token, LocalDateTime availableAt) {
            AlarmNotificationOutbox delivery = claimed(id, token);
            if (delivery == null) return 0;
            delivery.setStatus(AlarmNotificationOutboxStatus.RETRY);
            delivery.setAvailableAt(availableAt);
            delivery.setClaimToken(null);
            return 1;
        }

        @Override public synchronized int markDead(Long id, String token) {
            AlarmNotificationOutbox delivery = claimed(id, token);
            if (delivery == null) return 0;
            delivery.setStatus(AlarmNotificationOutboxStatus.DEAD);
            delivery.setClaimToken(null);
            delivery.setAvailableAt(null);
            return 1;
        }

        @Override public int deleteByRuleId(Long ruleId) { return 0; }
        @Override public int deleteByRuleIds(java.util.List<Long> ruleIds) { return 0; }

        private AlarmNotificationOutbox claimed(Long id, String token) {
            return deliveries.stream()
                    .filter(delivery -> delivery.getId().equals(id)
                            && delivery.getStatus() == AlarmNotificationOutboxStatus.PROCESSING
                            && token.equals(delivery.getClaimToken()))
                    .findFirst().orElse(null);
        }

        @Override
        public AlarmNotificationOutboxCounts selectCountsByHistoryId(Long historyId) {
            selectCountsCalls++;
            List<AlarmNotificationOutbox> historyDeliveries = deliveries.stream()
                    .filter(delivery -> delivery.getHistoryId().equals(historyId))
                    .toList();
            int sent = (int) historyDeliveries.stream()
                    .filter(delivery -> delivery.getStatus() == AlarmNotificationOutboxStatus.SENT)
                    .count();
            int dead = (int) historyDeliveries.stream()
                    .filter(delivery -> delivery.getStatus() == AlarmNotificationOutboxStatus.DEAD)
                    .count();
            return new AlarmNotificationOutboxCounts(historyDeliveries.size(), sent, dead);
        }
    }

    private static class InMemoryHistoryDao implements AlarmHistoryV2Dao {
        private AlarmHistoryV2 history;
        private int selectForUpdateCalls;
        private int updateContextCalls;

        @Override public void insert(AlarmHistoryV2 history) { this.history = history; }
        @Override public void updateContext(Long id, String context) {
            updateContextCalls++;
            history.setContext(context);
        }
        @Override public AlarmHistoryV2 selectById(Long id) { return history; }
        @Override public AlarmHistoryV2 selectByIdForUpdate(Long id) {
            selectForUpdateCalls++;
            return history;
        }
        @Override public List<AlarmHistoryV2> selectByRuleId(Long ruleId, int limit) { return List.of(history); }
        @Override public void deleteByRuleId(Long ruleId) { }
        @Override public void deleteByRuleIds(java.util.List<Long> ruleIds) { }
        @Override public int deleteOlderThan(LocalDateTime threshold, int limit) { return 0; }
    }

    private static class InMemoryStateDao implements AlarmStateDao {
        private AlarmState state;

        @Override public AlarmState selectByRuleId(Long ruleId) { return state; }
        @Override public void upsert(AlarmState state) { this.state = state; }
        @Override public int updateLastNotifiedAt(Long ruleId, LocalDateTime notifiedAt) {
            state.setLastNotifiedAt(notifiedAt);
            return 1;
        }
        @Override public void deleteByRuleId(Long ruleId) { }
        @Override public void deleteByRuleIds(java.util.List<Long> ruleIds) { }
    }

    private static class NoOpTransactionManager extends AbstractPlatformTransactionManager {
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) { }
        @Override protected void doRollback(DefaultTransactionStatus status) { }
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
