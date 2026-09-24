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
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.ConditionEvaluator;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationService;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationChannelConfigParser;
import com.navercorp.pinpoint.alarm.sender.AlarmSendException;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmEventType;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutboxCounts;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutboxStatus;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.alarm.vo.AlarmStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmEvaluationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private StubAlarmStateDao stateDao;
    private StubAlarmHistoryV2Dao historyDao;
    private StubAlarmNotificationOutboxDao outboxDao;
    private StubAlarmNotificationService notificationService;
    private TrackingTransactionManager transactionManager;
    private AlarmEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        stateDao = new StubAlarmStateDao();
        historyDao = new StubAlarmHistoryV2Dao();
        outboxDao = new StubAlarmNotificationOutboxDao();
        notificationService = new StubAlarmNotificationService(objectMapper);
        transactionManager = new TrackingTransactionManager();
        AlarmEventPersistenceService persistenceService = new AlarmEventPersistenceService(
                lockingRuleDao(), stateDao, historyDao, outboxDao,
                notificationService, transactionManager, objectMapper);
        evaluationService = new AlarmEvaluationService(stateDao, persistenceService, new ConditionEvaluator());
    }

    @Test
    void firstFire_persistsStateHistoryAndOutboxWithoutInlineSend() throws Exception {
        AlarmRuleV2 rule = createRule(1L, null);
        rule.setDescription("Test description");
        rule.setTemplateItemId(770L);
        rule.setTemplateId(77L);
        rule.setTemplateName("Test Template");
        rule.setFilters(List.of());
        rule.setOverrideSeverity(true);

        boolean fired = evaluationService.evaluate(rule, metricQueryService(150.0));

        assertTrue(fired);
        assertEquals(AlarmStatus.FIRING, stateDao.lastState.getStatus());
        assertNotNull(stateDao.lastState.getLastNotificationEnqueuedAt());
        assertNull(stateDao.lastState.getLastNotifiedAt());
        AlarmHistoryV2 history = historyDao.inserted.get(0);
        assertEquals(rule.getId(), history.getRuleId());
        assertEquals(AlarmEventType.FIRED, history.getEventType());
        assertEquals("[CRITICAL] Test Rule", history.getMessage());
        assertNotNull(history.getContext());
        assertEquals(1, outboxDao.inserted.size());
        AlarmNotificationOutbox delivery = outboxDao.inserted.get(0);
        assertEquals(history.getId(), delivery.getHistoryId());
        assertEquals(10L, delivery.getChannelId());
        assertEquals(AlarmMethodType.EMAIL, delivery.getMethodType());
        assertEquals("{}", delivery.getPayload());
        assertEquals(AlarmNotificationOutboxStatus.PENDING, delivery.getStatus());
        assertEquals(0, delivery.getAttemptCount());
        assertEquals(stateDao.lastState.getLastNotificationEnqueuedAt(), delivery.getAvailableAt());
        assertEquals(0, notificationService.deliverCount);
        JsonNode context = objectMapper.readTree(history.getContext());
        assertEquals(1, context.path("notification").path("pending").asInt());
        assertEquals(0, context.path("notification").path("sent").asInt());
        JsonNode effectiveRule = context.path("effective_rule");
        assertEquals(1L, effectiveRule.path("id").asLong());
        assertEquals("Test Rule", effectiveRule.path("name").asText());
        assertEquals("Test description", effectiveRule.path("description").asText());
        assertEquals("CRITICAL", effectiveRule.path("severity").asText());
        assertEquals("AGENT_STAT", effectiveRule.path("data_source").asText());
        assertEquals(77L, effectiveRule.path("template_id").asLong());
        assertEquals("Test Template", effectiveRule.path("template_name").asText());
        assertEquals("test-service", effectiveRule.path("service_name").asText());
        assertEquals("test-app", effectiveRule.path("application_name").asText());
        assertEquals(300, effectiveRule.path("check_interval_sec").asInt());
        assertEquals(3600, effectiveRule.path("action_interval_sec").asInt());
        assertEquals("LEAF", effectiveRule.path("conditions").path("type").asText());
        assertTrue(effectiveRule.path("filters").isArray());
        assertTrue(effectiveRule.path("enabled").asBoolean());
        assertEquals("severity", effectiveRule.path("override_keys").get(0).asText());
        assertEquals(1, transactionManager.commitCount);
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                transactionManager.lastDefinition.getPropagationBehavior());
    }

    @Test
    void alreadyFiring_usesLastEnqueuedTimeForThrottle() {
        AlarmState existing = new AlarmState(1L);
        existing.setStatus(AlarmStatus.FIRING);
        existing.setLastNotificationEnqueuedAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        existing.setLastNotifiedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2));
        stateDao.presetState = existing;

        boolean fired = evaluationService.evaluate(createRule(1L, null), metricQueryService(150.0));

        assertTrue(fired);
        assertTrue(historyDao.inserted.isEmpty());
        assertTrue(outboxDao.inserted.isEmpty());
    }

    @Test
    void alreadyFiring_enqueuesAgainAfterActionInterval() {
        AlarmState existing = new AlarmState(1L);
        existing.setStatus(AlarmStatus.FIRING);
        existing.setLastNotificationEnqueuedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(2));
        stateDao.presetState = existing;

        evaluationService.evaluate(createRule(1L, null), metricQueryService(150.0));

        assertEquals(1, historyDao.inserted.size());
        assertEquals(1, outboxDao.inserted.size());
    }

    // A firing rule whose check broke and came back is the same episode throughout -- it was
    // never resolved -- so the alert someone already has still throttles the repeat.
    @Test
    void firingAgainWithinAnUnresolvedEpisodeIsThrottled() {
        AlarmState stillTheSameEpisode = new AlarmState(1L);
        stillTheSameEpisode.setStatus(AlarmStatus.CHECK_FAILED);
        stillTheSameEpisode.setLastFiredAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(6));
        stillTheSameEpisode.setLastNotificationEnqueuedAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        stateDao.presetState = stillTheSameEpisode;

        boolean fired = evaluationService.evaluate(createRule(1L, null), metricQueryService(150.0));

        assertTrue(fired);
        assertEquals(AlarmStatus.FIRING, stateDao.lastState.getStatus());
        assertTrue(historyDao.inserted.isEmpty());
        assertTrue(outboxDao.inserted.isEmpty());
    }

    // ... but once it resolves, the episode is over and its throttle must go with it. The
    // stamp is only ever written by a firing and was never cleared anywhere, so it used to be
    // read against the next episode: one failed check on the way back in is enough to make
    // recordFired consult it, and a genuinely new alert is dropped for the rest of an
    // interval it never spent.
    @Test
    void aResolveEndsTheEpisodeSoTheNextFiringIsNotThrottled() {
        AlarmState firing = new AlarmState(1L);
        firing.setStatus(AlarmStatus.FIRING);
        firing.setLastNotificationEnqueuedAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        stateDao.presetState = firing;

        evaluationService.evaluate(createRule(1L, null), metricQueryService(0.0));
        assertNull(stateDao.lastState.getLastNotificationEnqueuedAt(),
                "the resolve must take the episode's throttle with it");

        // Checks break, then the condition returns -- a new episode arriving through a status
        // that is not NORMAL, which is exactly the path that consults the stamp.
        AlarmState brokenAfterResolve = stateDao.lastState;
        brokenAfterResolve.setStatus(AlarmStatus.CHECK_FAILED);
        stateDao.presetState = brokenAfterResolve;
        historyDao.inserted.clear();
        outboxDao.inserted.clear();

        boolean fired = evaluationService.evaluate(createRule(1L, null), metricQueryService(150.0));

        assertTrue(fired);
        assertEquals(1, historyDao.inserted.size(), "the new alert must not be throttled away");
        assertEquals(AlarmEventType.FIRED, historyDao.inserted.get(0).getEventType());
        assertEquals(1, outboxDao.inserted.size());
    }

    @Test
    void resolved_regularRuleRecordsResolvedHistory() {
        AlarmState existing = new AlarmState(1L);
        existing.setStatus(AlarmStatus.FIRING);
        stateDao.presetState = existing;

        boolean fired = evaluationService.evaluate(
                createRule(1L, null), metricQueryService(10.0));

        assertFalse(fired);
        assertEquals(AlarmStatus.NORMAL, stateDao.lastState.getStatus());
        AlarmHistoryV2 history = historyDao.inserted.get(0);
        assertEquals(1L, history.getRuleId());
        assertEquals(AlarmEventType.RESOLVED, history.getEventType());
        assertEquals("[RESOLVED] Test Rule", history.getMessage());
        assertNull(history.getContext());
        assertTrue(outboxDao.inserted.isEmpty());
    }

    @Test
    void resolvedNewGroupRuleResetsSilently() {
        AlarmState existing = new AlarmState(1L);
        existing.setStatus(AlarmStatus.FIRING);
        stateDao.presetState = existing;

        boolean fired = evaluationService.evaluate(
                createRule(1L, AlarmCondition.Trigger.NEW_GROUP), metricQueryService(0.0));

        assertFalse(fired);
        assertEquals(AlarmStatus.NORMAL, stateDao.lastState.getStatus());
        assertTrue(historyDao.inserted.isEmpty());
    }

    // Every evaluation failure lands here now. Nothing that gets this far is the rule
    // owner's to fix -- an authoring mistake is refused when the rule is saved -- so it is
    // written down for an operator to find and no message goes out.
    @Test
    void anEvaluationFailureIsRecordedWithoutNotifyingAnyone() {
        AlarmRuleV2 rule = createRule(1L, null);
        rule.setCheckIntervalSec(60);

        evaluationService.handleEvaluationFailed(
                rule, new IllegalStateException("Pinot query failed"));

        assertEquals(AlarmStatus.CHECK_FAILED, stateDao.lastState.getStatus());
        assertNotNull(stateDao.lastState.getLastCheckedAt());
        // The rule's own interval, not the fallback -- 60 rather than the fixture's 300, which
        // would otherwise be indistinguishable from DEFAULT_CHECK_FAILURE_RETRY_SEC.
        assertEquals(60, Duration.between(stateDao.lastState.getLastCheckedAt(),
                stateDao.lastState.getNextCheckAt()).toSeconds());
        assertNull(stateDao.lastState.getLastNotificationEnqueuedAt());
        assertNull(notificationService.lastPreparedRule);
        assertTrue(outboxDao.inserted.isEmpty());

        // The row is the only place the cause survives, so it has to carry it.
        assertEquals(1, historyDao.inserted.size());
        AlarmHistoryV2 recorded = historyDao.inserted.get(0);
        assertEquals(AlarmEventType.CHECK_FAILED, recorded.getEventType());
        assertTrue(recorded.getMessage().contains("Pinot query failed"),
                () -> "message must name the failure, was: " + recorded.getMessage());
        assertTrue(recorded.getContext().contains("IllegalStateException"),
                () -> "context must name the exception type, was: " + recorded.getContext());
    }

    // A backend that stays down is re-checked every interval. One row per episode, not per
    // tick, or the rule's own FIRED and RESOLVED rows fall off the only screen showing them.
    @Test
    void aFailureThatPersistsIsRecordedOnce() {
        AlarmRuleV2 rule = createRule(1L, null);

        evaluationService.handleEvaluationFailed(rule, new IllegalStateException("down"));
        AlarmState afterFirst = stateDao.lastState;
        LocalDateTime firstNextCheck = afterFirst.getNextCheckAt();
        stateDao.presetState = afterFirst;
        evaluationService.handleEvaluationFailed(rule, new IllegalStateException("still down"));

        assertEquals(1, historyDao.inserted.size(), "the second tick must not add a row");
        // Skipping the row must not skip the state write with it. Asserting on the status
        // alone cannot see that: presetState is the very object the first tick produced, so
        // it already reads CHECK_FAILED whether or not the second tick wrote anything. A
        // schedule that stops moving leaves the rule due forever, and the sweep spins on it.
        assertEquals(2, stateDao.upsertCount, "every tick must still move the schedule");
        assertTrue(stateDao.lastState.getNextCheckAt().isAfter(firstNextCheck));
    }

    // The raw row has no interval of its own -- alarm_rule_v2 has no such column -- so every
    // failure on the resolve path arrives with null and would otherwise unbox into an NPE.
    @Test
    void aRuleWithNoIntervalFallsBackToTheRetryDefault() {
        AlarmRuleV2 rule = createRule(1L, null);
        rule.setCheckIntervalSec(null);

        evaluationService.handleEvaluationFailed(rule, new IllegalStateException("boom"));

        AlarmState state = stateDao.lastState;
        assertEquals(300, Duration.between(state.getLastCheckedAt(), state.getNextCheckAt()).toSeconds());
    }

    // Coming out of CHECK_FAILED is worth a row whatever the rule was doing before it broke.
    // A resolve enqueues nothing, so the row costs an operator nothing to read and is the
    // only marker for when the outage ended.
    @Test
    void aRuleComingOutOfCheckFailedResolves() {
        AlarmState broken = new AlarmState(1L);
        broken.setStatus(AlarmStatus.CHECK_FAILED);
        stateDao.presetState = broken;

        boolean fired = evaluationService.evaluate(createRule(1L, null), metricQueryService(0.0));

        assertFalse(fired);
        assertEquals(AlarmStatus.NORMAL, stateDao.lastState.getStatus());
        assertEquals(1, historyDao.inserted.size());
        assertEquals(AlarmEventType.RESOLVED, historyDao.inserted.get(0).getEventType());
        assertTrue(outboxDao.inserted.isEmpty(), "a resolve tells nobody");
    }

    // The one-shot exemption keeps a NEW_GROUP rule from resolving a firing it never held
    // open, but it does not apply to a broken check -- that is a state it really sat in.
    // (The other half, that such a rule does not resolve out of FIRING, is
    // resolvedNewGroupRuleResetsSilently.)
    @Test
    void aOneShotRuleResolvesOutOfCheckFailed() {
        AlarmState broken = new AlarmState(1L);
        broken.setStatus(AlarmStatus.CHECK_FAILED);
        stateDao.presetState = broken;

        boolean fired = evaluationService.evaluate(
                createRule(1L, AlarmCondition.Trigger.NEW_GROUP), metricQueryService(0.0));

        assertFalse(fired);
        assertEquals(1, historyDao.inserted.size());
        assertEquals(AlarmEventType.RESOLVED, historyDao.inserted.get(0).getEventType());
    }

    // #1 -- a firing rule whose backend dies. The episode's stamps have to survive the
    // failure, or the repeat firing on the way back out re-notifies immediately.
    @Test
    void aFiringRuleThatBreaksKeepsItsEpisode() {
        LocalDateTime firedAt = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5);
        AlarmState firing = new AlarmState(1L);
        firing.setStatus(AlarmStatus.FIRING);
        firing.setLastFiredAt(firedAt);
        firing.setLastNotificationEnqueuedAt(firedAt);
        stateDao.presetState = firing;

        evaluationService.handleEvaluationFailed(
                createRule(1L, null), new IllegalStateException("backend down"));

        assertEquals(AlarmStatus.CHECK_FAILED, stateDao.lastState.getStatus());
        assertEquals(1, historyDao.inserted.size());
        assertEquals(AlarmEventType.CHECK_FAILED, historyDao.inserted.get(0).getEventType());
        assertEquals(firedAt, stateDao.lastState.getLastFiredAt());
        assertEquals(firedAt, stateDao.lastState.getLastNotificationEnqueuedAt(),
                "a failed check must not reset the episode's throttle");
    }

    // #5 -- the mirror of firingAgainWithinAnUnresolvedEpisodeIsThrottled: a rule that has
    // never notified anyone has nothing to throttle against, and must fire freely.
    @Test
    void firingOutOfCheckFailedWithNoPriorNotificationIsNotThrottled() {
        AlarmState neverNotified = new AlarmState(1L);
        neverNotified.setStatus(AlarmStatus.CHECK_FAILED);
        stateDao.presetState = neverNotified;

        boolean fired = evaluationService.evaluate(createRule(1L, null), metricQueryService(150.0));

        assertTrue(fired);
        assertEquals(1, historyDao.inserted.size());
        assertEquals(AlarmEventType.FIRED, historyDao.inserted.get(0).getEventType());
        assertEquals(1, outboxDao.inserted.size());
    }

    // A rule that was already NORMAL has come out of nothing.
    @Test
    void aNormalRuleThatStaysNormalWritesNothing() {
        AlarmState normal = new AlarmState(1L);
        normal.setStatus(AlarmStatus.NORMAL);
        stateDao.presetState = normal;

        evaluationService.evaluate(createRule(1L, null), metricQueryService(0.0));

        assertEquals(AlarmStatus.NORMAL, stateDao.lastState.getStatus());
        assertTrue(historyDao.inserted.isEmpty());
    }

    // A rule that failed to resolve is the raw row and has no name, so the label falls back
    // to the id rather than titling the record "[CHECK_FAILED] null".
    @Test
    void anUnnamedRuleIsRecordedByItsId() {
        AlarmRuleV2 rule = createRule(7L, null);
        rule.setName(null);

        evaluationService.handleEvaluationFailed(rule, new IllegalStateException("boom"));

        assertTrue(historyDao.inserted.get(0).getMessage().contains("rule#7"),
                () -> "was: " + historyDao.inserted.get(0).getMessage());
    }

    @Test
    void firedPreparationFailureStillCommitsStateAndHistory() throws Exception {
        notificationService.preparationFailure = new AlarmSendException("recipient lookup failed");

        boolean fired = evaluationService.evaluate(createRule(1L, null), metricQueryService(150.0));

        assertTrue(fired);
        assertEquals(AlarmStatus.FIRING, stateDao.lastState.getStatus());
        assertEquals(AlarmEventType.FIRED, historyDao.inserted.get(0).getEventType());
        assertTrue(outboxDao.inserted.isEmpty());
        JsonNode notification = objectMapper.readTree(historyDao.inserted.get(0).getContext())
                .path("notification");
        assertEquals(1, notification.path("preparation_failed").asInt());
        assertEquals("recipient lookup failed",
                notification.path("preparation_failures").get(0).path("message").asText());
    }

    @Test
    void partialPreparationFailurePersistsSuccessfulDeliveryAndFailureDetails() throws Exception {
        notificationService.nextPreparation = new AlarmNotificationService.PreparationResult(
                List.of(new AlarmNotificationService.PreparedDelivery(20L, AlarmMethodType.SMS, "{}")),
                List.of(new AlarmNotificationService.PreparationFailure(10L, "email preparation failed")),
                null);

        evaluationService.evaluate(createRule(1L, null), metricQueryService(150.0));

        assertEquals(1, outboxDao.inserted.size());
        assertEquals(20L, outboxDao.inserted.get(0).getChannelId());
        JsonNode notification = objectMapper.readTree(historyDao.inserted.get(0).getContext())
                .path("notification");
        assertEquals(1, notification.path("pending").asInt());
        assertEquals(1, notification.path("preparation_failed").asInt());
        assertEquals(10L,
                notification.path("preparation_failures").get(0).path("channel_id").asLong());
    }

    @Test
    void metricQueryServiceReceivesCurrentState() {
        AlarmState existing = new AlarmState(1L);
        existing.setStatus(AlarmStatus.NORMAL);
        existing.setLastCheckedAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        stateDao.presetState = existing;
        List<AlarmState> captured = new ArrayList<>();
        MetricQueryService metricQueryService = new MetricQueryService() {
            @Override public AlarmDataSource getDataSource() {
                return TestAlarmDataSource.AGENT_STAT;
            }

            @Override
            public MetricQueryResult query(AlarmRuleV2 rule,
                                           List<AlarmCondition> conditions,
                                           List<com.navercorp.pinpoint.alarm.vo.AlarmFilter> filters,
                                           AlarmState state) {
                captured.add(state);
                return MetricQueryResult.of(Map.of(MetricQueryKey.from(conditions.get(0)), 0.0));
            }
        };

        evaluationService.evaluate(createRule(1L, null), metricQueryService);

        assertEquals(existing, captured.get(0));
    }

    private AlarmRuleV2 createRule(Long id, AlarmCondition.Trigger trigger) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(id);
        rule.setName("Test Rule");
        rule.setSeverity(AlarmSeverity.CRITICAL);
        rule.setDataSource(TestAlarmDataSource.AGENT_STAT.name());
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setServiceName("test-service");
        rule.setApplicationName("test-app");
        rule.setCheckIntervalSec(300);
        rule.setActionIntervalSec(3600);
        rule.setEnabled(true);

        AlarmCondition condition = new AlarmCondition();
        condition.setType(AlarmCondition.Type.LEAF);
        condition.setTrigger(trigger);
        condition.setMetric(trigger == AlarmCondition.Trigger.NEW_GROUP ? "deadlock_count" : "error_count");
        condition.setOp(trigger == AlarmCondition.Trigger.NEW_GROUP
                ? AlarmCondition.ComparisonOp.GT : AlarmCondition.ComparisonOp.GTE);
        condition.setThreshold(trigger == AlarmCondition.Trigger.NEW_GROUP ? 0.0 : 100.0);
        condition.setWindowSec(300);
        condition.setAggregation(AlarmCondition.Aggregation.COUNT);
        rule.setConditions(condition);
        return rule;
    }

    private MetricQueryService metricQueryService(double value) {
        return new MetricQueryService() {
            @Override public AlarmDataSource getDataSource() {
                return TestAlarmDataSource.AGENT_STAT;
            }

            @Override
            public MetricQueryResult query(AlarmRuleV2 rule,
                                           List<AlarmCondition> conditions,
                                           List<com.navercorp.pinpoint.alarm.vo.AlarmFilter> filters,
                                           AlarmState state) {
                return MetricQueryResult.of(Map.of(MetricQueryKey.from(conditions.get(0)), value));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private AlarmRuleV2Dao lockingRuleDao() {
        return (AlarmRuleV2Dao) Proxy.newProxyInstance(
                AlarmRuleV2Dao.class.getClassLoader(), new Class<?>[]{AlarmRuleV2Dao.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("selectRuleByIdForUpdate")) {
                        AlarmRuleV2 locked = new AlarmRuleV2();
                        locked.setId((Long) args[0]);
                        return locked;
                    }
                    if (List.class.isAssignableFrom(method.getReturnType())) {
                        return List.of();
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private static <T> T noop(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (List.class.isAssignableFrom(method.getReturnType())) {
                        return List.of();
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    return null;
                });
    }

    private static class StubAlarmNotificationService extends AlarmNotificationService {
        private PreparationResult nextPreparation = new PreparationResult(
                List.of(new PreparedDelivery(10L, AlarmMethodType.EMAIL, "{}")), null);
        private RuntimeException preparationFailure;
        private AlarmRuleV2 lastPreparedRule;
        private int deliverCount;

        private StubAlarmNotificationService(ObjectMapper objectMapper) {
            super(noop(AlarmChannelBindingDao.class), noop(AlarmNotificationChannelDao.class),
                    List.of(), objectMapper, new AlarmNotificationChannelConfigParser(objectMapper));
        }

        @Override
        public PreparationResult prepareNotifications(
                AlarmRuleV2 rule, MetricQueryResult metricResults) {
            lastPreparedRule = rule;
            if (preparationFailure != null) {
                throw preparationFailure;
            }
            return nextPreparation;
        }

        @Override
        public void deliver(AlarmNotificationOutbox delivery) {
            deliverCount++;
        }
    }

    private static class StubAlarmStateDao implements AlarmStateDao {
        private AlarmState presetState;
        private AlarmState lastState;
        private int upsertCount;

        @Override public AlarmState selectByRuleId(Long ruleId) { return presetState; }
        @Override public void upsert(AlarmState state) { lastState = state; upsertCount++; }
        @Override public int updateLastNotifiedAt(Long ruleId, LocalDateTime notifiedAt) { return 1; }
        @Override public void deleteByRuleId(Long ruleId) { }
        @Override public void deleteByRuleIds(java.util.List<Long> ruleIds) { }
    }

    private static class StubAlarmHistoryV2Dao implements AlarmHistoryV2Dao {
        private final List<AlarmHistoryV2> inserted = new ArrayList<>();
        private long nextId = 1;

        @Override
        public void insert(AlarmHistoryV2 history) {
            history.setId(nextId++);
            inserted.add(history);
        }

        @Override public void updateContext(Long id, String context) { }
        @Override public AlarmHistoryV2 selectById(Long id) { return null; }
        @Override public AlarmHistoryV2 selectByIdForUpdate(Long id) { return null; }
        @Override public List<AlarmHistoryV2> selectByRuleId(Long ruleId, int limit) { return inserted; }
        @Override public void deleteByRuleId(Long ruleId) { }
        @Override public void deleteByRuleIds(java.util.List<Long> ruleIds) { }
        @Override public int deleteOlderThan(LocalDateTime threshold, int limit) { return 0; }
    }

    private static class StubAlarmNotificationOutboxDao implements AlarmNotificationOutboxDao {
        private final List<AlarmNotificationOutbox> inserted = new ArrayList<>();
        private long nextId = 1;

        @Override public void insert(AlarmNotificationOutbox delivery) {
            delivery.setId(nextId++);
            inserted.add(delivery);
        }
        @Override public List<Long> selectClaimCandidateIds(LocalDateTime now, int limit) { return List.of(); }
        @Override public int claimAvailable(String token, LocalDateTime now, LocalDateTime lease, List<Long> ids) { return 0; }
        @Override public List<AlarmNotificationOutbox> selectByClaimToken(String token) { return List.of(); }
        @Override public int markSent(Long id, String token) { return 0; }
        @Override public int markRetry(Long id, String token, LocalDateTime availableAt) { return 0; }
        @Override public int markDead(Long id, String token) { return 0; }
        @Override public int deleteByRuleId(Long ruleId) { return 0; }
        @Override public int deleteByRuleIds(java.util.List<Long> ruleIds) { return 0; }
        @Override
        public AlarmNotificationOutboxCounts selectCountsByHistoryId(Long historyId) {
            return new AlarmNotificationOutboxCounts(0, 0, 0);
        }
    }

    private static class TrackingTransactionManager extends AbstractPlatformTransactionManager {
        private int commitCount;
        private TransactionDefinition lastDefinition;

        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) {
            lastDefinition = definition;
        }
        @Override protected void doCommit(DefaultTransactionStatus status) { commitCount++; }
        @Override protected void doRollback(DefaultTransactionStatus status) { }
    }
}
