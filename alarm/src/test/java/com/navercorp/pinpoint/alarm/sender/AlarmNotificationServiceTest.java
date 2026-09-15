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
package com.navercorp.pinpoint.alarm.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelBinding;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmNotificationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private StubAlarmChannelBindingDao bindingDao;
    private StubAlarmNotificationChannelDao channelDao;
    private RecordingAlarmSender emailSender;
    private RecordingAlarmSender smsSender;
    private AlarmNotificationService service;

    @BeforeEach
    void setUp() {
        bindingDao = new StubAlarmChannelBindingDao();
        channelDao = new StubAlarmNotificationChannelDao();
        emailSender = new RecordingAlarmSender(AlarmMethodType.EMAIL);
        smsSender = new RecordingAlarmSender(AlarmMethodType.SMS);
        service = new AlarmNotificationService(
                bindingDao, channelDao, List.of(emailSender, smsSender), objectMapper,
                new AlarmNotificationChannelConfigParser(objectMapper));
    }

    @Test
    void prepareNotifications_createsOneImmutableSnapshotPerChannelWithoutSending() throws Exception {
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 10L);
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 20L);
        channelDao.add(channel(10L, AlarmMethodType.EMAIL, "group-email"));
        channelDao.add(channel(20L, AlarmMethodType.SMS, "group-sms"));

        AlarmNotificationService.PreparationResult result =
                service.prepareNotifications(rule(1L), MetricQueryResult.empty());

        assertEquals(2, result.deliveries().size());
        assertEquals("email-title", objectMapper.readTree(result.deliveries().get(0).payload())
                .path("subject").asText());
        assertEquals(1, emailSender.prepareCount);
        assertEquals(1, smsSender.prepareCount);
        assertEquals(0, emailSender.sendCount);
        assertEquals(0, smsSender.sendCount);
    }

    @Test
    void prepareNotifications_passesTypedChannelConfigToSender() {
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 10L);
        AlarmNotificationChannel channel = channel(10L, AlarmMethodType.EMAIL, "group-email");
        channel.setConfig("""
                {"title":"custom-title","template":"custom-template"}
                """);
        channelDao.add(channel);

        AlarmNotificationService.PreparationResult result =
                service.prepareNotifications(rule(1L), MetricQueryResult.empty());

        assertEquals(1, result.deliveries().size());
        assertEquals(new AlarmNotificationChannelConfig(
                null, "custom-title", "custom-template"), emailSender.lastChannelConfig);
    }

    @Test
    void prepareNotifications_nullAndBlankChannelConfigsUseEmptyConfig() {
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 10L);
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 20L);
        AlarmNotificationChannel nullConfig = channel(10L, AlarmMethodType.EMAIL, "group-email-1");
        AlarmNotificationChannel blankConfig = channel(20L, AlarmMethodType.EMAIL, "group-email-2");
        blankConfig.setConfig(" \t\n");
        channelDao.add(nullConfig);
        channelDao.add(blankConfig);

        AlarmNotificationService.PreparationResult result =
                service.prepareNotifications(rule(1L), MetricQueryResult.empty());

        assertEquals(2, result.deliveries().size());
        assertEquals(List.of(
                AlarmNotificationChannelConfig.empty(),
                AlarmNotificationChannelConfig.empty()), emailSender.channelConfigs);
    }

    @Test
    void prepareNotifications_malformedChannelConfigFailsOnlyThatChannel() {
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 10L);
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 20L);
        AlarmNotificationChannel malformed = channel(10L, AlarmMethodType.EMAIL, "group-email-1");
        malformed.setConfig("{not-json");
        AlarmNotificationChannel valid = channel(20L, AlarmMethodType.EMAIL, "group-email-2");
        valid.setConfig("{}");
        channelDao.add(malformed);
        channelDao.add(valid);

        AlarmNotificationService.PreparationResult result =
                service.prepareNotifications(rule(1L), MetricQueryResult.empty());

        assertEquals(1, result.deliveries().size());
        assertEquals(20L, result.deliveries().get(0).channelId());
        assertEquals(1, result.failures().size());
        assertEquals(10L, result.failures().get(0).channelId());
        assertFalse(result.failures().get(0).message().isBlank());
        assertEquals(1, emailSender.prepareCount);
    }

    @Test
    void prepareNotifications_usesTemplateBindingsForTemplateLinkedRule() {
        AlarmRuleV2 rule = rule(1L);
        rule.setTemplateId(100L);
        bindingDao.bind(AlarmChannelOwnerType.TEMPLATE, 100L, 10L);
        channelDao.add(channel(10L, AlarmMethodType.EMAIL, "group-email"));

        AlarmNotificationService.PreparationResult result = service.prepareNotifications(rule, MetricQueryResult.empty());

        assertEquals(1, result.deliveries().size());
    }

    @Test
    void prepareNotifications_withoutBindingsRecordsSkippedReason() {
        AlarmNotificationService.PreparationResult result =
                service.prepareNotifications(rule(1L), MetricQueryResult.empty());

        assertTrue(result.deliveries().isEmpty());
        assertEquals("No notification channel binding found for RULE owner: owner_id=1",
                result.skippedReason());
    }

    @Test
    void prepareNotifications_missingChannelRecordsFailureAndKeepsValidDelivery() {
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 99L);
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 10L);
        channelDao.add(channel(10L, AlarmMethodType.EMAIL, "group-email"));

        AlarmNotificationService.PreparationResult result =
                service.prepareNotifications(rule(1L), MetricQueryResult.empty());

        assertEquals(1, result.deliveries().size());
        assertEquals(1, result.failures().size());
        assertEquals(99L, result.failures().get(0).channelId());
        assertTrue(result.failures().get(0).message().contains("channel_id=99"));
    }

    @Test
    void prepareNotifications_missingSenderRecordsFailure() {
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 30L);
        channelDao.add(channel(30L, AlarmMethodType.WEBHOOK, "https://example.test/hook"));

        AlarmNotificationService.PreparationResult result =
                service.prepareNotifications(rule(1L), MetricQueryResult.empty());

        assertTrue(result.deliveries().isEmpty());
        assertEquals(1, result.failures().size());
        assertTrue(result.failures().get(0).message().contains("WEBHOOK"));
    }

    @Test
    void prepareNotifications_senderFailureDoesNotBlockOtherChannels() {
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 10L);
        bindingDao.bind(AlarmChannelOwnerType.RULE, 1L, 20L);
        channelDao.add(channel(10L, AlarmMethodType.EMAIL, "group-email"));
        channelDao.add(channel(20L, AlarmMethodType.SMS, "group-sms"));
        emailSender.prepareFailure = new AlarmSendException("email preparation failed");

        AlarmNotificationService.PreparationResult result =
                service.prepareNotifications(rule(1L), MetricQueryResult.empty());

        assertEquals(1, result.deliveries().size());
        assertEquals(20L, result.deliveries().get(0).channelId());
        assertEquals(1, result.failures().size());
        assertEquals(10L, result.failures().get(0).channelId());
        assertEquals("email preparation failed", result.failures().get(0).message());
    }

    @Test
    void deliver_usesOnlyPersistedSnapshot() throws Exception {
        AlarmDeliveryPayload payload = AlarmDeliveryPayload.forEmail(
                "persisted-title", "<p>persisted-body</p>",
                List.of("a@example.test"));
        AlarmNotificationOutbox outbox = new AlarmNotificationOutbox();
        outbox.setId(501L);
        outbox.setMethodType(AlarmMethodType.EMAIL);
        outbox.setPayload(objectMapper.writeValueAsString(payload));

        service.deliver(outbox);

        assertEquals(1, emailSender.sendCount);
        assertEquals(501L, emailSender.lastDelivery.getId());
        assertEquals("persisted-title", emailSender.lastPayload.subject());
        assertEquals(List.of("a@example.test"), emailSender.lastPayload.destinations());
    }

    @Test
    void deliver_rejectsIncompleteSnapshotBeforeSenderInvocation() {
        AlarmNotificationOutbox outbox = new AlarmNotificationOutbox();
        outbox.setId(501L);
        outbox.setMethodType(AlarmMethodType.EMAIL);
        outbox.setPayload("""
                {"content":"persisted-body","destinations":["a@example.test"]}
                """);

        assertThrows(AlarmSendException.class, () -> service.deliver(outbox));
        assertEquals(0, emailSender.sendCount);
    }

    private AlarmRuleV2 rule(Long id) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(id);
        rule.setName("rule-" + id);
        return rule;
    }

    private AlarmNotificationChannel channel(Long id, AlarmMethodType methodType, String destination) {
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setId(id);
        channel.setMethodType(methodType);
        channel.setDestination(destination);
        return channel;
    }

    private static class StubAlarmChannelBindingDao implements AlarmChannelBindingDao {
        private final List<AlarmChannelBinding> bindings = new ArrayList<>();

        void bind(AlarmChannelOwnerType ownerType, Long ownerId, Long channelId) {
            bindings.add(new AlarmChannelBinding(ownerType, ownerId, channelId));
        }

        @Override
        public List<AlarmChannelBinding> selectByOwner(AlarmChannelOwnerType ownerType, Long ownerId) {
            return bindings.stream()
                    .filter(binding -> binding.getOwnerType() == ownerType
                            && binding.getOwnerId().equals(ownerId))
                    .toList();
        }

        @Override public void insert(AlarmChannelBinding binding) { bindings.add(binding); }
        @Override public void delete(AlarmChannelOwnerType ownerType, Long ownerId, Long channelId) { }
        @Override public void deleteByOwner(AlarmChannelOwnerType ownerType, Long ownerId) { }
        @Override public void deleteByChannelId(Long channelId) { }
        @Override public void deleteRuleBindingsByRuleIds(java.util.List<Long> ruleIds) { }
        @Override public void deleteByUserGroupDestination(String userGroupId) { }
        @Override public boolean existsByChannelId(Long channelId) { return false; }
    }

    private static class StubAlarmNotificationChannelDao implements AlarmNotificationChannelDao {
        private final List<AlarmNotificationChannel> channels = new ArrayList<>();

        void add(AlarmNotificationChannel channel) {
            channels.add(channel);
        }

        @Override
        public List<AlarmNotificationChannel> selectByIds(List<Long> ids) {
            return channels.stream().filter(channel -> ids.contains(channel.getId())).toList();
        }

        @Override public void insert(AlarmNotificationChannel channel) { channels.add(channel); }
        @Override public void update(AlarmNotificationChannel channel) { }
        @Override public void delete(Long id) { }
        @Override public AlarmNotificationChannel selectById(Long id) { return null; }
        @Override public AlarmNotificationChannel selectByIdForUpdate(Long id) { return null; }
        @Override public List<AlarmNotificationChannel> selectAll() { return List.copyOf(channels); }
        @Override public List<AlarmNotificationChannel> selectByService(String serviceName) { return List.of(); }
        @Override public List<AlarmNotificationChannel> selectByIdsWithUsage(List<Long> ids) { return selectByIds(ids); }
        @Override public List<AlarmNotificationChannel> selectByRuleId(Long ruleId) { return List.of(); }
        @Override public List<AlarmNotificationChannel> selectByServiceName(String serviceName) { return List.of(); }
        @Override public boolean existsUserGroup(String userGroupId) { return false; }
        @Override public List<AlarmNotificationChannel> selectByUserGroupDestinationForUpdate(String userGroupId) { return List.of(); }
        @Override public void deleteByUserGroupDestination(String userGroupId) { }
    }

    private static class RecordingAlarmSender implements AlarmSender {
        private final AlarmMethodType methodType;
        private int prepareCount;
        private int sendCount;
        private RuntimeException prepareFailure;
        private AlarmNotificationOutbox lastDelivery;
        private AlarmDeliveryPayload lastPayload;
        private AlarmNotificationChannelConfig lastChannelConfig;
        private final List<AlarmNotificationChannelConfig> channelConfigs = new ArrayList<>();

        private RecordingAlarmSender(AlarmMethodType methodType) {
            this.methodType = methodType;
        }

        @Override
        public AlarmMethodType getMethodType() {
            return methodType;
        }

        @Override
        public AlarmDeliveryPayload prepare(AlarmRuleV2 rule,
                                            AlarmNotificationChannel channel,
                                            AlarmNotificationChannelConfig channelConfig,
                                            MetricQueryResult metricResults) {
            prepareCount++;
            if (prepareFailure != null) {
                throw prepareFailure;
            }
            lastChannelConfig = channelConfig;
            channelConfigs.add(channelConfig);
            String prefix = methodType.name().toLowerCase();
            if (methodType == AlarmMethodType.SMS) {
                return AlarmDeliveryPayload.forSms(prefix + "-title\n" + prefix + "-body", List.of());
            }
            return AlarmDeliveryPayload.forEmail(
                    prefix + "-title", prefix + "-content", List.of());
        }

        @Override
        public void send(AlarmNotificationOutbox delivery, AlarmDeliveryPayload payload) {
            sendCount++;
            lastDelivery = delivery;
            lastPayload = payload;
        }
    }
}
