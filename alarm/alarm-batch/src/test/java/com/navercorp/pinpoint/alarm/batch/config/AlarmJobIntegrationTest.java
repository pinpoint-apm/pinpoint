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
package com.navercorp.pinpoint.alarm.batch.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationService;
import com.navercorp.pinpoint.alarm.service.AlarmEventPersistenceService;
import com.navercorp.pinpoint.alarm.service.AlarmNotificationOutboxClaimService;
import com.navercorp.pinpoint.alarm.service.AlarmNotificationResultService;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelBinding;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.alarm.vo.AlarmStatus;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBatchTest
@SpringJUnitConfig
@Testcontainers
@ContextConfiguration(classes = AlarmJobIntegrationTestConfig.class)
@TestPropertySource(locations = "classpath:application-alarm-test.yml")
class AlarmJobIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = AlarmJobIntegrationTestConfig.MYSQL;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private AlarmRuleV2Dao alarmRuleV2Dao;

    @Autowired
    private AlarmRuleLocalConfigDao alarmRuleLocalConfigDao;

    @Autowired
    private AlarmTemplateDao alarmTemplateDao;

    @Autowired
    private AlarmTemplateItemDao alarmTemplateItemDao;

    @Autowired
    private AlarmNotificationChannelDao alarmNotificationChannelDao;

    @Autowired
    private AlarmChannelBindingDao alarmChannelBindingDao;

    @Autowired
    private AlarmStateDao alarmStateDao;

    @Autowired
    private AlarmHistoryV2Dao alarmHistoryV2Dao;

    @Autowired
    private AlarmNotificationOutboxDao alarmNotificationOutboxDao;

    @Autowired
    private AlarmNotificationService mockNotificationService;

    @Autowired
    private AlarmEventPersistenceService alarmEventPersistenceService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MetricQueryService mockMetricQueryService;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private JdbcTemplate jdbc;
    private static final String SERVICE_NAME = "test-service";
    private static final String APPLICATION_NAME = "test-app";

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
        // clear alarm data, leave Spring Batch meta tables intact
        jdbc.execute("DELETE FROM pinpoint.alarm_notification_outbox");
        jdbc.execute("DELETE FROM pinpoint.alarm_history_v2");
        jdbc.execute("DELETE FROM pinpoint.alarm_state");
        jdbc.execute("DELETE FROM pinpoint.alarm_channel_binding");
        jdbc.execute("DELETE FROM pinpoint.alarm_rule_local_config");
        jdbc.execute("DELETE FROM pinpoint.alarm_notification_channel");
        jdbc.execute("DELETE FROM pinpoint.alarm_rule_v2");
        jdbc.execute("DELETE FROM pinpoint.alarm_template_item");
        jdbc.execute("DELETE FROM pinpoint.alarm_template");
        reset(mockNotificationService);
        reset(mockMetricQueryService);
        when(mockNotificationService.prepareNotifications(any(), any()))
                .thenReturn(preparedDelivery(10L));
        when(mockMetricQueryService.getDataSource()).thenReturn(IntegrationTestAlarmDataSource.PRIMARY);
    }

    // ---- happy path ----

    @Test
    void alarmFires_firstTime_stateIsFiringAndHistoryInserted() throws Exception {
        AlarmRuleV2 rule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 150.0));

        JobExecution exec = jobLauncherTestUtils.launchJob();

        assertEquals(ExitStatus.COMPLETED, exec.getExitStatus());

        AlarmState state = alarmStateDao.selectByRuleId(rule.getId());
        assertNotNull(state);
        assertEquals(AlarmStatus.FIRING, state.getStatus());
        assertNotNull(state.getLastFiredAt());
        assertNotNull(state.getLastNotificationEnqueuedAt());
        assertNull(state.getLastNotifiedAt());

        int historyCount = countHistory(rule.getId(), "FIRED");
        assertEquals(1, historyCount);

        verify(mockNotificationService, times(1)).prepareNotifications(any(), any());
    }

    @Test
    void channelUsage_excludesSoftDeletedTemplateAcrossQueryShapes() {
        AlarmTemplateItem activeTemplate =
                insertTemplate("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        insertTemplateRule(activeTemplate);
        insertTemplateRule(activeTemplate, false);
        AlarmTemplateItem deletedTemplate =
                insertTemplate("affected_user_count", AlarmCondition.ComparisonOp.GTE, 50.0);
        insertTemplateRule(deletedTemplate);
        AlarmRuleV2 standaloneRule =
                insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 200.0);
        AlarmNotificationChannel channel = insertWebhookChannel();

        bindChannel(AlarmChannelOwnerType.TEMPLATE, activeTemplate.getTemplateId(), channel.getId());
        bindChannel(AlarmChannelOwnerType.TEMPLATE, deletedTemplate.getTemplateId(), channel.getId());
        bindChannel(AlarmChannelOwnerType.RULE, standaloneRule.getId(), channel.getId());

        assertChannelUsage(alarmNotificationChannelDao.selectById(channel.getId()), 2, 4, 3);

        assertEquals(1, alarmTemplateDao.markDeleted(deletedTemplate.getTemplateId()));

        assertChannelUsage(
                findChannel(alarmNotificationChannelDao.selectByServiceName("test-service"), channel.getId()),
                1, 3, 2);
        assertChannelUsage(alarmNotificationChannelDao.selectById(channel.getId()), 1, 3, 2);
        assertChannelUsage(
                findChannel(
                        alarmNotificationChannelDao.selectByIdsWithUsage(List.of(channel.getId())),
                        channel.getId()),
                1, 3, 2);
    }

    @Test
    void alarmDoesNotFire_conditionNotMet_stateIsNormalNoHistory() throws Exception {
        AlarmRuleV2 rule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 30.0)); // below threshold

        jobLauncherTestUtils.launchJob();

        AlarmState state = alarmStateDao.selectByRuleId(rule.getId());
        assertNotNull(state);
        assertEquals(AlarmStatus.NORMAL, state.getStatus());

        assertEquals(0, countHistory(rule.getId(), "FIRED"));
        verify(mockNotificationService, never()).prepareNotifications(any(), any());
    }

    @Test
    void alarmResolves_fromFiringToNormal_resolvedHistoryInserted() throws Exception {
        AlarmRuleV2 rule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        // Pre-seed FIRING state
        seedFiringState(rule.getId(), nowUtc().minusHours(2));

        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 10.0)); // now below threshold

        jobLauncherTestUtils.launchJob();

        AlarmState state = alarmStateDao.selectByRuleId(rule.getId());
        assertEquals(AlarmStatus.NORMAL, state.getStatus());

        assertEquals(0, countHistory(rule.getId(), "FIRED"));
        assertEquals(1, countHistory(rule.getId(), "RESOLVED"));
        verify(mockNotificationService, never()).prepareNotifications(any(), any());
    }

    @Test
    void alarmAlreadyFiring_withinActionInterval_noRepeatNotification() throws Exception {
        AlarmRuleV2 rule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        // actionIntervalSec = 3600 (1h); last notified 10 minutes ago
        seedFiringState(rule.getId(), nowUtc().minusMinutes(10));

        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 200.0));

        jobLauncherTestUtils.launchJob();

        AlarmState state = alarmStateDao.selectByRuleId(rule.getId());
        assertEquals(AlarmStatus.FIRING, state.getStatus());

        // Still in action interval → no new history / no new notification
        assertEquals(0, countHistory(rule.getId(), "FIRED"));
        verify(mockNotificationService, never()).prepareNotifications(any(), any());
    }

    @Test
    void alarmAlreadyFiring_pastActionInterval_notificationRepeated() throws Exception {
        AlarmRuleV2 rule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        // Last notified 2 hours ago; actionIntervalSec = 3600
        seedFiringState(rule.getId(), nowUtc().minusHours(2));

        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 200.0));

        jobLauncherTestUtils.launchJob();

        assertEquals(1, countHistory(rule.getId(), "FIRED"));
        verify(mockNotificationService, times(1)).prepareNotifications(any(), any());
    }

    @Test
    void disabledRule_skippedByTasklet() throws Exception {
        AlarmRuleV2 rule = insertDisabledRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 999.0));

        jobLauncherTestUtils.launchJob();

        // selectEnabledRules() should not return disabled rules
        AlarmState state = alarmStateDao.selectByRuleId(rule.getId());
        // state will be null — evaluation never ran
        assertEquals(0, countHistory(rule.getId(), "FIRED"));
        verify(mockNotificationService, never()).prepareNotifications(any(), any());
    }

    @Test
    void multipleRules_eachEvaluatedIndependently() throws Exception {
        AlarmRuleV2 rule1 = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        AlarmRuleV2 rule2 = insertRule("affected_user_count", AlarmCondition.ComparisonOp.GTE, 50.0);

        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    AlarmRuleV2 evaluated = invocation.getArgument(0);
                    return rule1.getId().equals(evaluated.getId())
                            ? metricResults("error_count", 150.0)
                            : metricResults("affected_user_count", 20.0);
                });

        jobLauncherTestUtils.launchJob();

        assertEquals(AlarmStatus.FIRING, alarmStateDao.selectByRuleId(rule1.getId()).getStatus());
        assertEquals(AlarmStatus.NORMAL, alarmStateDao.selectByRuleId(rule2.getId()).getStatus());
        verify(mockNotificationService, times(1)).prepareNotifications(any(), any());
    }

    @Test
    void templateLinkedRule_inheritsTemplateConfigInBatch() throws Exception {
        AlarmTemplateItem template = insertTemplate("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        AlarmRuleV2 rule = insertTemplateRule(template);
        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    AlarmRuleV2 effectiveRule = invocation.getArgument(0);
                    assertEquals(template.getTemplateId(), effectiveRule.getTemplateId());
                    assertEquals("Bundle error_count", effectiveRule.getTemplateName());
                    assertEquals(template.getName(), effectiveRule.getTemplateItemName());
                    assertEquals(template.getSeverity(), effectiveRule.getSeverity());
                    assertEquals("error_count", effectiveRule.getConditions().getMetric());
                    assertEquals(0, effectiveRule.getCheckIntervalSec());
                    return metricResults("error_count", 150.0);
                });

        jobLauncherTestUtils.launchJob();

        assertEquals(AlarmStatus.FIRING, alarmStateDao.selectByRuleId(rule.getId()).getStatus());
        assertEquals(1, countHistory(rule.getId(), "FIRED"));
        verify(mockNotificationService, times(1)).prepareNotifications(any(), any());
    }

    @Test
    void softDeletedItemRulesAreInvisibleToTheApplyDuplicateCheck() {
        // Removing one item from a bundle only soft-deletes it, leaving its rules until
        // the cleanup batch runs. Those leftovers must not make the bundle look like it
        // is still applied, or the application could never re-apply it.
        AlarmTemplateItem removedItem = insertTemplate("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        AlarmRuleV2 ruleOfRemovedItem = insertTemplateRule(removedItem);
        Long templateId = removedItem.getTemplateId();

        assertEquals(
                List.of(ruleOfRemovedItem.getId()),
                alarmRuleV2Dao
                        .selectRulesByTemplateIdAndApplicationForUpdate(templateId, SERVICE_NAME, APPLICATION_NAME,
                                AlarmApplication.TYPE_JAVASCRIPT)
                        .stream()
                        .map(AlarmRuleV2::getId)
                        .toList());

        assertEquals(1, alarmTemplateItemDao.markDeleted(removedItem.getId()));

        assertTrue(alarmRuleV2Dao
                .selectRulesByTemplateIdAndApplicationForUpdate(templateId, SERVICE_NAME, APPLICATION_NAME,
                                AlarmApplication.TYPE_JAVASCRIPT)
                .isEmpty());
    }

    @Test
    void deletedTemplateLinkedRule_isExcludedFromEvaluationAndRuleQueries() throws Exception {
        AlarmTemplateItem template = insertTemplate("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        AlarmRuleV2 rule = insertTemplateRule(template);
        alarmTemplateDao.markDeleted(template.getTemplateId());

        jobLauncherTestUtils.launchJob();

        assertEquals(AlarmStatus.NORMAL, alarmStateDao.selectByRuleId(rule.getId()).getStatus());
        assertEquals(0, countHistory(rule.getId(), "FIRED"));
        assertTrue(alarmRuleV2Dao.selectRulesByApplication(SERVICE_NAME, APPLICATION_NAME,
                AlarmApplication.TYPE_JAVASCRIPT).isEmpty());
        verify(mockMetricQueryService, never()).query(any(), any(), any(), any());
        verify(mockNotificationService, never()).prepareNotifications(any(), any());
    }

    @Test
    void templateResolveFailure_recordsCheckFailedAndContinuesOtherRules() throws Exception {
        AlarmRuleV2 brokenRule = insertBrokenTemplateRule();
        AlarmRuleV2 healthyRule = insertRule(
                "error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 150.0));

        JobExecution exec = jobLauncherTestUtils.launchJob();

        assertEquals(ExitStatus.COMPLETED, exec.getExitStatus());
        AlarmState brokenState = alarmStateDao.selectByRuleId(brokenRule.getId());
        assertNotNull(brokenState);
        assertEquals(AlarmStatus.CHECK_FAILED, brokenState.getStatus());
        assertNotNull(brokenState.getLastCheckedAt());
        // A rule nobody can evaluate is the operator's to find, not the owner's to be told
        // about: it is written to history so there is something to find, and nothing is enqueued.
        assertEquals(1, countHistory(brokenRule.getId(), "CHECK_FAILED"));
        assertNull(brokenState.getLastNotificationEnqueuedAt());
        assertNull(brokenState.getLastNotifiedAt());
        assertEquals(0, countOutbox(brokenRule.getId()));
        assertEquals(AlarmStatus.FIRING, alarmStateDao.selectByRuleId(healthyRule.getId()).getStatus());
        assertEquals(1, countHistory(healthyRule.getId(), "FIRED"));
        verify(mockNotificationService, times(1)).prepareNotifications(any(), any());
    }

    @Test
    void metricCollectionFailure_recordsCheckFailedAndContinuesOtherRules() throws Exception {
        AlarmRuleV2 brokenRule = insertRule(
                "error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        AlarmRuleV2 healthyRule = insertRule(
                "affected_user_count", AlarmCondition.ComparisonOp.GTE, 50.0);
        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    AlarmRuleV2 rule = invocation.getArgument(0);
                    if (brokenRule.getId().equals(rule.getId())) {
                        throw new IllegalStateException("metric query failed");
                    }
                    return metricResults("affected_user_count", 100.0);
                });

        JobExecution exec = jobLauncherTestUtils.launchJob();

        assertEquals(ExitStatus.COMPLETED, exec.getExitStatus());
        AlarmState brokenState = alarmStateDao.selectByRuleId(brokenRule.getId());
        assertEquals(AlarmStatus.CHECK_FAILED, brokenState.getStatus());
        assertNull(brokenState.getLastNotificationEnqueuedAt());
        assertEquals(1, countHistory(brokenRule.getId(), "CHECK_FAILED"));
        assertEquals(AlarmStatus.FIRING, alarmStateDao.selectByRuleId(healthyRule.getId()).getStatus());
        assertEquals(1, countHistory(healthyRule.getId(), "FIRED"));
        verify(mockNotificationService, times(1)).prepareNotifications(any(), any());
    }

    // More than one evaluation process can share these tables, one per set of data sources it
    // has the stores for. A rule belonging to another one must come back from neither the
    // query nor anything this process writes: recording it as failed would move its next check
    // and tell its owner their rule is broken, from a process that was never meant to read it.
    @Test
    void aRuleOfAnotherProcessDataSource_isLeftUntouchedEntirely() throws Exception {
        AlarmRuleV2 otherProcessRule = insertRuleWithDataSource("OWNED_BY_ANOTHER_PROCESS");
        AlarmRuleV2 healthyRule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 150.0));
        AlarmState before = alarmStateDao.selectByRuleId(otherProcessRule.getId());

        JobExecution exec = jobLauncherTestUtils.launchJob();

        assertEquals(ExitStatus.COMPLETED, exec.getExitStatus());

        AlarmState after = alarmStateDao.selectByRuleId(otherProcessRule.getId());
        assertEquals(before.getStatus(), after.getStatus());
        assertEquals(before.getNextCheckAt(), after.getNextCheckAt(),
                "another process schedules this rule; this one must not move it");
        assertEquals(0, countHistory(otherProcessRule.getId(), "CHECK_FAILED"));

        assertEquals(AlarmStatus.FIRING, alarmStateDao.selectByRuleId(healthyRule.getId()).getStatus());
        assertEquals(1, countHistory(healthyRule.getId(), "FIRED"));
        // Only the rule this process owns produced a notification.
        verify(mockNotificationService, times(1)).prepareNotifications(any(), any());
    }

    @Test
    void outboxInsertFailure_rollsBackTheFiringWriteAndDoesNotSkipOtherRules() throws Exception {
        // Both rules fire; only one of them cannot have its delivery enqueued. A check
        // failure no longer enqueues anything, so firing is the path this can happen on.
        AlarmRuleV2 blockedRule = insertRule(
                "error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        AlarmRuleV2 healthyRule = insertRule(
                "error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        when(mockMetricQueryService.query(any(), any(), any(), any()))
                .thenReturn(metricResults("error_count", 150.0));
        when(mockNotificationService.prepareNotifications(any(), any()))
                .thenAnswer(invocation -> {
                    AlarmRuleV2 notificationRule = invocation.getArgument(0);
                    return blockedRule.getId().equals(notificationRule.getId())
                            ? duplicatePreparedDelivery(10L)
                            : preparedDelivery(20L);
                });

        JobExecution exec = jobLauncherTestUtils.launchJob();

        assertEquals(ExitStatus.COMPLETED, exec.getExitStatus());
        // The firing write rolled back whole, and the tasklet then recorded the rule as one
        // whose check did not complete. The status alone does not prove the rollback -- the
        // CHECK_FAILED write sets it either way -- so the two columns only recordFired touches
        // are what says the firing transaction left nothing behind.
        AlarmState blockedState = alarmStateDao.selectByRuleId(blockedRule.getId());
        assertEquals(AlarmStatus.CHECK_FAILED, blockedState.getStatus());
        assertNull(blockedState.getLastFiredAt());
        assertNull(blockedState.getLastNotificationEnqueuedAt());
        assertEquals(0, countHistory(blockedRule.getId(), "FIRED"));
        assertEquals(1, countHistory(blockedRule.getId(), "CHECK_FAILED"));
        assertEquals(0, countOutbox(blockedRule.getId()));
        assertEquals(AlarmStatus.FIRING, alarmStateDao.selectByRuleId(healthyRule.getId()).getStatus());
        assertEquals(1, countHistory(healthyRule.getId(), "FIRED"));
        assertEquals(1, countOutbox(healthyRule.getId()));
        verify(mockNotificationService, times(2)).prepareNotifications(any(), any());
    }

    @Test
    void eventPersistenceSurvivesOuterTransactionRollbackAndDoesNotSendInline() {
        AlarmRuleV2 rule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        LocalDateTime eventTime = nowUtc().withNano(0);
        TransactionTemplate outer = new TransactionTemplate(transactionManager);

        outer.executeWithoutResult(status -> {
            alarmEventPersistenceService.recordFired(
                    rule, metricResults("error_count", 150.0), List.of(rule.getConditions()), eventTime);
            status.setRollbackOnly();
        });

        AlarmState state = alarmStateDao.selectByRuleId(rule.getId());
        assertEquals(AlarmStatus.FIRING, state.getStatus());
        assertEquals(eventTime, state.getLastNotificationEnqueuedAt());
        assertNull(state.getLastNotifiedAt());
        assertEquals(1, countHistory(rule.getId(), "FIRED"));
        assertEquals(1, countOutbox(rule.getId()));
        verify(mockNotificationService, never()).deliver(any());
    }

    @Test
    void expiredLeaseCanBeReclaimedButActiveLeaseCannot() {
        AlarmNotificationOutbox delivery = insertOutbox(100L, 10L, nowUtc());
        AlarmNotificationOutboxClaimService claimService =
                new AlarmNotificationOutboxClaimService(alarmNotificationOutboxDao, transactionManager);
        LocalDateTime now = nowUtc().withNano(0);

        List<AlarmNotificationOutbox> first = claimService.claim(10, now, Duration.ofMinutes(1));
        List<AlarmNotificationOutbox> whileLeased =
                claimService.claim(10, now.plusSeconds(30), Duration.ofMinutes(1));
        List<AlarmNotificationOutbox> afterExpiry =
                claimService.claim(10, now.plusSeconds(61), Duration.ofMinutes(1));

        assertEquals(List.of(delivery.getId()), first.stream().map(AlarmNotificationOutbox::getId).toList());
        assertTrue(whileLeased.isEmpty());
        assertEquals(List.of(delivery.getId()),
                afterExpiry.stream().map(AlarmNotificationOutbox::getId).toList());
        assertEquals(2, afterExpiry.get(0).getAttemptCount());
    }

    @Test
    void concurrentDispatchersCannotClaimSameDelivery() throws Exception {
        AlarmNotificationOutbox delivery = insertOutbox(100L, 10L, nowUtc());
        AlarmNotificationOutboxClaimService firstService =
                new AlarmNotificationOutboxClaimService(alarmNotificationOutboxDao, transactionManager);
        AlarmNotificationOutboxClaimService secondService =
                new AlarmNotificationOutboxClaimService(alarmNotificationOutboxDao, transactionManager);
        LocalDateTime now = nowUtc().withNano(0);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<List<AlarmNotificationOutbox>> first = executor.submit(() -> {
                ready.countDown();
                start.await();
                return firstService.claim(1, now, Duration.ofMinutes(1));
            });
            Future<List<AlarmNotificationOutbox>> second = executor.submit(() -> {
                ready.countDown();
                start.await();
                return secondService.claim(1, now, Duration.ofMinutes(1));
            });
            ready.await();
            start.countDown();

            List<AlarmNotificationOutbox> combined = new java.util.ArrayList<>();
            combined.addAll(first.get());
            combined.addAll(second.get());
            assertEquals(1, combined.size());
            assertEquals(delivery.getId(), combined.get(0).getId());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void duplicateHistoryChannelDeliveryIsRejected() {
        insertOutbox(100L, 10L, nowUtc());

        assertThrows(DuplicateKeyException.class,
                () -> insertOutbox(100L, 10L, nowUtc()));
    }

    @Test
    void deleteOutboxByRuleIdDeletesOnlyTheSelectedRuleDeliveries() {
        AlarmRuleV2 selectedRule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        AlarmRuleV2 otherRule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 200.0);
        insertOutbox(insertHistory(selectedRule).getId(), 10L, nowUtc());
        insertOutbox(insertHistory(otherRule).getId(), 20L, nowUtc());

        assertEquals(1, alarmNotificationOutboxDao.deleteByRuleId(selectedRule.getId()));

        assertEquals(0, countOutbox(selectedRule.getId()));
        assertEquals(1, countOutbox(otherRule.getId()));
    }

    @Test
    void deleteOutboxByApplicationDeletesOnlyTheSelectedApplicationDeliveries() {
        AlarmRuleV2 selectedRule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        insertOutbox(insertHistory(selectedRule).getId(), 10L, nowUtc());

        AlarmRuleV2 otherRule = buildRule("error_count", AlarmCondition.ComparisonOp.GTE, 200.0, true);
        otherRule.setServiceName("other-service");
        otherRule.setApplicationName("other-app");
        otherRule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        alarmRuleV2Dao.insertRule(otherRule);
        insertOutbox(insertHistory(otherRule).getId(), 20L, nowUtc());

        List<Long> ruleIds = alarmRuleV2Dao.selectRuleIdsByApplication(SERVICE_NAME, APPLICATION_NAME,
                AlarmApplication.TYPE_JAVASCRIPT);
        assertEquals(List.of(selectedRule.getId()), ruleIds);
        assertEquals(1, alarmNotificationOutboxDao.deleteByRuleIds(ruleIds));

        assertEquals(0, countOutbox(selectedRule.getId()));
        assertEquals(1, countOutbox(otherRule.getId()));
    }

    @Test
    void sentResultUpdatesHistorySummaryAndActualNotificationTime() throws Exception {
        AlarmRuleV2 rule = insertRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        AlarmHistoryV2 history = new AlarmHistoryV2();
        history.setRuleId(rule.getId());
        history.setEventType(com.navercorp.pinpoint.alarm.vo.AlarmEventType.FIRED);
        history.setMessage("fired");
        history.setContext("{\"notification\":{\"sent\":0,\"failed\":1,\"pending\":2}}");
        alarmHistoryV2Dao.insert(history);
        insertOutbox(history.getId(), 10L, nowUtc());
        AlarmNotificationOutbox dead = insertOutbox(history.getId(), 20L, nowUtc());
        jdbc.update("UPDATE pinpoint.alarm_notification_outbox " +
                        "SET status = 'DEAD', available_at = NULL WHERE id = ?",
                dead.getId());
        AlarmNotificationOutbox retry = insertOutbox(history.getId(), 30L, nowUtc().plusHours(1));
        jdbc.update("UPDATE pinpoint.alarm_notification_outbox SET status = 'RETRY' WHERE id = ?",
                retry.getId());

        AlarmNotificationOutboxClaimService claimService =
                new AlarmNotificationOutboxClaimService(alarmNotificationOutboxDao, transactionManager);
        AlarmNotificationOutbox claimed = claimService.claim(
                1, nowUtc(), Duration.ofMinutes(1)).get(0);
        AlarmNotificationResultService resultService = new AlarmNotificationResultService(
                alarmNotificationOutboxDao, alarmStateDao, alarmHistoryV2Dao,
                transactionManager, objectMapper);
        LocalDateTime sentAt = nowUtc().withNano(0);

        assertTrue(resultService.markSent(claimed, sentAt));

        assertEquals(sentAt, alarmStateDao.selectByRuleId(rule.getId()).getLastNotifiedAt());
        JsonNode context = objectMapper.readTree(alarmHistoryV2Dao.selectById(history.getId()).getContext());
        assertEquals(1, context.path("notification").path("sent").asInt());
        assertEquals(1, context.path("notification").path("failed").asInt());
        assertEquals(1, context.path("notification").path("pending").asInt());
    }

    @Test
    void jobCompletes_whenNoRulesExist() throws Exception {
        // empty table
        JobExecution exec = jobLauncherTestUtils.launchJob();
        assertEquals(ExitStatus.COMPLETED, exec.getExitStatus());
    }

    // ---- helpers ----

    private AlarmRuleV2 insertRule(String metric, AlarmCondition.ComparisonOp op, double threshold) {
        AlarmRuleV2 rule = buildRule(metric, op, threshold, true);
        alarmRuleV2Dao.insertRule(rule);
        alarmRuleLocalConfigDao.upsert(localConfig(rule));
        seedNormalState(rule.getId());
        return rule;
    }

    private AlarmRuleV2 insertRuleWithDataSource(String dataSource) {
        AlarmRuleV2 rule = buildRule("error_count", AlarmCondition.ComparisonOp.GTE, 100.0, true);
        rule.setDataSource(dataSource);
        alarmRuleV2Dao.insertRule(rule);
        alarmRuleLocalConfigDao.upsert(localConfig(rule));
        seedNormalState(rule.getId());
        return rule;
    }

    private AlarmTemplateItem insertTemplate(String metric, AlarmCondition.ComparisonOp op, double threshold) {
        AlarmTemplate template = new AlarmTemplate();
        template.setServiceName("test-service");
        template.setName("Bundle " + metric);
        alarmTemplateDao.insert(template);

        AlarmTemplateItem item = new AlarmTemplateItem();
        item.setTemplateId(template.getId());
        item.setName("Template " + metric);
        item.setSeverity(AlarmSeverity.WARNING);
        item.setDataSource(IntegrationTestAlarmDataSource.PRIMARY.name());
        item.setCheckIntervalSec(0);
        item.setActionIntervalSec(3600);
        item.setConditions(condition(metric, op, threshold));
        item.setFilters(List.of());
        alarmTemplateItemDao.insert(item);
        return item;
    }

    private AlarmRuleV2 insertTemplateRule(AlarmTemplateItem templateItem) {
        return insertTemplateRule(templateItem, true);
    }

    private AlarmRuleV2 insertTemplateRule(AlarmTemplateItem templateItem, boolean enabled) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setName("Template Rule");
        rule.setDataSource(templateItem.getDataSource());
        rule.setTemplateItemId(templateItem.getId());
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setServiceName("test-service");
        rule.setApplicationName("test-app");
        rule.setEnabled(enabled);
        alarmRuleV2Dao.insertRule(rule);
        if (enabled) {
            seedNormalState(rule.getId());
        } else {
            seedDisabledState(rule.getId());
        }
        return rule;
    }

    private AlarmNotificationChannel insertWebhookChannel() {
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setServiceName("test-service");
        channel.setChannelName("Test Webhook");
        channel.setMethodType(AlarmMethodType.WEBHOOK);
        channel.setDestination("https://example.com/alarm");
        channel.setConfig("{}");
        alarmNotificationChannelDao.insert(channel);
        return channel;
    }

    private void bindChannel(AlarmChannelOwnerType ownerType, Long ownerId, Long channelId) {
        alarmChannelBindingDao.insert(new AlarmChannelBinding(ownerType, ownerId, channelId));
    }

    private AlarmNotificationChannel findChannel(List<AlarmNotificationChannel> channels, Long channelId) {
        return channels.stream()
                .filter(channel -> channelId.equals(channel.getId()))
                .findFirst()
                .orElse(null);
    }

    private void assertChannelUsage(AlarmNotificationChannel channel,
                                    int templateCount,
                                    int affectedRuleCount,
                                    int enabledAffectedRuleCount) {
        assertNotNull(channel);
        assertEquals(templateCount, channel.getTemplateCount());
        assertEquals(affectedRuleCount, channel.getAffectedRuleCount());
        assertEquals(enabledAffectedRuleCount, channel.getEnabledAffectedRuleCount());
    }

    private AlarmRuleV2 insertBrokenTemplateRule() {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setName("Broken Template Rule");
        rule.setDataSource(IntegrationTestAlarmDataSource.PRIMARY.name());
        rule.setTemplateItemId(999L);
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setServiceName("test-service");
        rule.setApplicationName("test-app");
        rule.setEnabled(true);
        alarmRuleV2Dao.insertRule(rule);
        seedNormalState(rule.getId());
        return rule;
    }

    private AlarmRuleV2 insertDisabledRule(String metric, AlarmCondition.ComparisonOp op, double threshold) {
        AlarmRuleV2 rule = buildRule(metric, op, threshold, false);
        alarmRuleV2Dao.insertRule(rule);
        alarmRuleLocalConfigDao.upsert(localConfig(rule));
        seedDisabledState(rule.getId());
        return rule;
    }

    private AlarmRuleV2 buildRule(String metric, AlarmCondition.ComparisonOp op, double threshold, boolean enabled) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setName("Test Rule " + metric);
        rule.setSeverity(AlarmSeverity.CRITICAL);
        rule.setDataSource(IntegrationTestAlarmDataSource.PRIMARY.name());
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setServiceName("test-service");
        rule.setApplicationName("test-app");
        rule.setCheckIntervalSec(0); // always re-evaluate in tests
        rule.setActionIntervalSec(3600);
        rule.setEnabled(enabled);

        rule.setConditions(condition(metric, op, threshold));
        rule.setFilters(List.of());

        return rule;
    }

    private AlarmCondition condition(String metric, AlarmCondition.ComparisonOp op, double threshold) {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric(metric);
        leaf.setOp(op);
        leaf.setThreshold(threshold);
        leaf.setWindowSec(300);
        leaf.setAggregation(AlarmCondition.Aggregation.COUNT);
        return leaf;
    }

    private AlarmRuleLocalConfig localConfig(AlarmRuleV2 rule) {
        AlarmRuleLocalConfig config = new AlarmRuleLocalConfig();
        config.setRuleId(rule.getId());
        config.setSeverity(rule.getSeverity());
        config.setCheckIntervalSec(rule.getCheckIntervalSec());
        config.setActionIntervalSec(rule.getActionIntervalSec());
        config.setConditions(rule.getConditions());
        config.setFilters(rule.getFilters());
        return config;
    }

    private MetricQueryResult metricResults(String metric, double value) {
        return MetricQueryResult.of(
                Map.of(new MetricQueryKey(metric, null, 300, AlarmCondition.Aggregation.COUNT), value));
    }

    private AlarmNotificationService.PreparationResult preparedDelivery(Long channelId) {
        return new AlarmNotificationService.PreparationResult(
                List.of(new AlarmNotificationService.PreparedDelivery(
                        channelId, AlarmMethodType.EMAIL, "{}")), null);
    }

    private AlarmNotificationService.PreparationResult duplicatePreparedDelivery(Long channelId) {
        AlarmNotificationService.PreparedDelivery delivery =
                new AlarmNotificationService.PreparedDelivery(
                        channelId, AlarmMethodType.EMAIL, "{}");
        return new AlarmNotificationService.PreparationResult(List.of(delivery, delivery), null);
    }

    private AlarmNotificationOutbox insertOutbox(Long historyId,
                                                  Long channelId,
                                                  LocalDateTime availableAt) {
        AlarmNotificationOutbox delivery = new AlarmNotificationOutbox();
        delivery.setHistoryId(historyId);
        delivery.setChannelId(channelId);
        delivery.setMethodType(AlarmMethodType.EMAIL);
        delivery.setPayload("{}");
        delivery.setAvailableAt(availableAt.withNano(0));
        alarmNotificationOutboxDao.insert(delivery);
        return delivery;
    }

    private AlarmHistoryV2 insertHistory(AlarmRuleV2 rule) {
        AlarmHistoryV2 history = new AlarmHistoryV2();
        history.setRuleId(rule.getId());
        history.setEventType(com.navercorp.pinpoint.alarm.vo.AlarmEventType.FIRED);
        history.setMessage("fired");
        history.setContext("{}");
        alarmHistoryV2Dao.insert(history);
        return history;
    }

    /**
     * Pre-seeds a FIRING state so tests can simulate already-firing scenarios.
     * Enqueue and actual notification timestamps are both set to {@code notifiedAt}.
     */
    private void seedFiringState(Long ruleId, LocalDateTime notifiedAt) {
        jdbc.update(
                "INSERT INTO pinpoint.alarm_state " +
                "(rule_id, status, last_checked_at, last_fired_at, " +
                "last_notification_enqueued_at, last_notified_at, next_check_at) " +
                "VALUES (?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status), " +
                "last_checked_at = VALUES(last_checked_at), " +
                "last_fired_at = VALUES(last_fired_at), " +
                "last_notification_enqueued_at = VALUES(last_notification_enqueued_at), " +
                "last_notified_at = VALUES(last_notified_at), " +
                "next_check_at = VALUES(next_check_at)",
                ruleId, "FIRING", notifiedAt, notifiedAt,
                notifiedAt, notifiedAt, nowUtc().minusMinutes(1)
        );
    }

    private void seedNormalState(Long ruleId) {
        jdbc.update(
                "INSERT INTO pinpoint.alarm_state (rule_id, status, next_check_at) VALUES (?,?,?)",
                ruleId, "NORMAL", nowUtc().minusMinutes(1)
        );
    }

    private void seedDisabledState(Long ruleId) {
        jdbc.update(
                "INSERT INTO pinpoint.alarm_state (rule_id, status, next_check_at) VALUES (?,?,?)",
                ruleId, "NORMAL", null
        );
    }

    private int countHistory(Long ruleId, String eventType) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pinpoint.alarm_history_v2 WHERE rule_id = ? AND event_type = ?",
                Integer.class, ruleId, eventType
        );
        return count != null ? count : 0;
    }

    private int countOutbox(Long ruleId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pinpoint.alarm_notification_outbox o " +
                        "INNER JOIN pinpoint.alarm_history_v2 h ON h.id = o.history_id " +
                        "WHERE h.rule_id = ?",
                Integer.class, ruleId);
        return count != null ? count : 0;
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
