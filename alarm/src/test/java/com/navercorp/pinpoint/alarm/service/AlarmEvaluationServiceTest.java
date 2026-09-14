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
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
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
        assertEquals("PRIMARY", effectiveRule.path("data_source").asText());
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

    @Test
    void firingAfterNotifiedCheckFailureUsesGlobalThrottle() {
        AlarmState existing = new AlarmState(1L);
        existing.setStatus(AlarmStatus.CHECK_FAILED);
        existing.setLastNotificationEnqueuedAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        stateDao.presetState = existing;

        boolean fired = evaluationService.evaluate(createRule(1L, null), metricQueryService(150.0));

        assertTrue(fired);
        assertEquals(AlarmStatus.FIRING, stateDao.lastState.getStatus());
        assertTrue(historyDao.inserted.isEmpty());
        assertTrue(outboxDao.inserted.isEmpty());
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

    @Test
    void checkFailedUsesSameOutboxFlow() throws Exception {
        AlarmRuleV2 rule = createRule(1L, null);
        rule.setCheckIntervalSec(600);
        rule.setActionIntervalSec(7200);
        List<AlarmFilter> filters = List.of(
                new AlarmFilter("environment", AlarmFilter.Op.EQ, "production"));
        rule.setFilters(filters);

        evaluationService.handleRuleConfigurationFailed(
                rule, new IllegalArgumentException("unsupported rule metric"));

        assertEquals(AlarmStatus.CHECK_FAILED, stateDao.lastState.getStatus());
        AlarmHistoryV2 history = historyDao.inserted.get(0);
        assertEquals(rule.getId(), history.getRuleId());
        assertEquals(AlarmEventType.CHECK_FAILED, history.getEventType());
        assertEquals("[CHECK_FAILED] Test Rule: unsupported rule metric", history.getMessage());
        assertEquals(1, outboxDao.inserted.size());
        assertEquals(0, notificationService.deliverCount);
        JsonNode context = objectMapper.readTree(history.getContext());
        assertEquals("unsupported rule metric", context.path("failure").path("message").asText());
        assertEquals(1, context.path("notification").path("pending").asInt());
        assertEquals(AlarmSeverity.CRITICAL, notificationService.lastPreparedRule.getSeverity());
        assertEquals(600, notificationService.lastPreparedRule.getCheckIntervalSec());
        assertEquals(7200, notificationService.lastPreparedRule.getActionIntervalSec());
        assertEquals(filters, notificationService.lastPreparedRule.getFilters());
    }

    @Test
    void checkFailedHistoryFallsBackToTheRuleIdWhenTheRawRuleHasNoName() {
        // A rule that failed to resolve is the raw row, and no rule stores its name there:
        // a bundle rule inherits it from the item, a standalone one keeps it in its local
        // config. Without the fallback every one of these reads "[CHECK_FAILED] null".
        AlarmRuleV2 rule = createRule(1L, null);
        rule.setName(null);

        evaluationService.handleRuleConfigurationFailed(
                rule, new IllegalArgumentException("templateItem must not be null"));

        AlarmHistoryV2 history = historyDao.inserted.get(0);
        assertEquals("[CHECK_FAILED] rule#1: templateItem must not be null", history.getMessage());
    }

    @Test
    void infrastructureFailureUpdatesStateWithoutUserNotification() {
        AlarmRuleV2 rule = createRule(1L, null);

        evaluationService.handleInfrastructureFailed(
                rule, new IllegalStateException("Pinot query failed"));

        assertEquals(AlarmStatus.CHECK_FAILED, stateDao.lastState.getStatus());
        assertNotNull(stateDao.lastState.getLastCheckedAt());
        assertNotNull(stateDao.lastState.getNextCheckAt());
        assertNull(stateDao.lastState.getLastNotificationEnqueuedAt());
        assertTrue(historyDao.inserted.isEmpty());
        assertTrue(outboxDao.inserted.isEmpty());
        assertNull(notificationService.lastPreparedRule);
    }

    @Test
    void checkFailedNotificationRuleUsesDefaultsAndPreservesRoutingFields() {
        AlarmRuleV2 rule = createRule(1L, null);
        rule.setName(null);
        rule.setSeverity(null);
        rule.setCheckIntervalSec(0);
        rule.setActionIntervalSec(null);
        rule.setFilters(null);
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setTemplateItemId(2020L);
        rule.setTemplateId(202L);
        rule.setTemplateName("Template 202");
        rule.setUpdatedAt(LocalDateTime.of(2026, 7, 20, 12, 0));
        rule.setOverrideSeverity(true);

        evaluationService.handleRuleConfigurationFailed(
                rule,
                new IllegalStateException("outer", new IllegalArgumentException("root failure")));

        AlarmRuleV2 notificationRule = notificationService.lastPreparedRule;
        assertNotNull(notificationRule);
        assertEquals(1L, notificationRule.getId());
        assertEquals("Alarm check failed: rule#1", notificationRule.getName());
        assertEquals("root failure", notificationRule.getDescription());
        assertEquals(AlarmSeverity.CRITICAL, notificationRule.getSeverity());
        assertEquals(rule.getDataSource(), notificationRule.getDataSource());
        assertEquals(AlarmApplication.TYPE_JAVASCRIPT, notificationRule.getApplicationType());
        assertEquals(202L, notificationRule.getTemplateId());
        assertEquals("Template 202", notificationRule.getTemplateName());
        assertEquals("test-service", notificationRule.getServiceName());
        assertEquals("test-app", notificationRule.getApplicationName());
        assertEquals(300, notificationRule.getCheckIntervalSec());
        assertEquals(3600, notificationRule.getActionIntervalSec());
        assertEquals(rule.getConditions(), notificationRule.getConditions());
        assertEquals(List.of(), notificationRule.getFilters());
        assertTrue(notificationRule.isEnabled());
        assertEquals(rule.getUpdatedAt(), notificationRule.getUpdatedAt());
        assertEquals(List.of(), notificationRule.getOverrideKeys());
    }

    @Test
    void checkFailedWithoutBindingsStoresSkippedReason() throws Exception {
        notificationService.nextPreparation = new AlarmNotificationService.PreparationResult(
                List.of(), "No notification channel binding found");

        evaluationService.handleRuleConfigurationFailed(
                createRule(1L, null), new IllegalStateException("failed"));

        JsonNode context = objectMapper.readTree(historyDao.inserted.get(0).getContext());
        assertEquals("No notification channel binding found",
                context.path("notification").path("skipped_reason").asText());
        assertTrue(outboxDao.inserted.isEmpty());
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
    void checkFailedPreparationFailureStillCommitsStateAndHistory() throws Exception {
        notificationService.preparationFailure = new AlarmSendException("formatter failed");

        evaluationService.handleRuleConfigurationFailed(
                createRule(1L, null), new IllegalStateException("metric query service unavailable"));

        assertEquals(AlarmStatus.CHECK_FAILED, stateDao.lastState.getStatus());
        assertEquals(AlarmEventType.CHECK_FAILED, historyDao.inserted.get(0).getEventType());
        assertTrue(outboxDao.inserted.isEmpty());
        JsonNode notification = objectMapper.readTree(historyDao.inserted.get(0).getContext())
                .path("notification");
        assertEquals(1, notification.path("preparation_failed").asInt());
        assertEquals("formatter failed",
                notification.path("preparation_failures").get(0).path("message").asText());
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
                return TestAlarmDataSource.PRIMARY;
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
        rule.setDataSource(TestAlarmDataSource.PRIMARY.name());
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setServiceName("test-service");
        rule.setApplicationName("test-app");
        rule.setCheckIntervalSec(300);
        rule.setActionIntervalSec(3600);
        rule.setEnabled(true);

        AlarmCondition condition = new AlarmCondition();
        condition.setType(AlarmCondition.Type.LEAF);
        condition.setTrigger(trigger);
        condition.setMetric(trigger == AlarmCondition.Trigger.NEW_GROUP ? "new_group_count" : "error_count");
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
                return TestAlarmDataSource.PRIMARY;
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

        @Override public AlarmState selectByRuleId(Long ruleId) { return presetState; }
        @Override public void upsert(AlarmState state) { lastState = state; }
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
