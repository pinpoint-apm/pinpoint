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
package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.validation.ConditionValidator;
import com.navercorp.pinpoint.alarm.validation.FilterKeyValidator;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.service.EffectiveAlarmRuleResolver;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleDetails;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class AlarmRuleServiceTest extends AlarmServiceTestSupport {

    @Test
    void createRuleUsesProvidedApplication() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);

        AlarmRuleV2 createdRule = service.createRule(rule);

        assertEquals(1, ruleDao.insertedRules.size());
        assertEquals(createdRule, ruleDao.insertedRules.get(0));
        assertEquals(11L, createdRule.getId());
        assertEquals(AlarmApplication.TYPE_JAVASCRIPT, createdRule.getApplicationType());
        assertEquals(SERVICE_NAME, createdRule.getServiceName());
    }

    @Test
    void createStandaloneRuleMovesItsTextIntoTheLocalConfig() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingLocalConfigDao localConfigDao = new RecordingLocalConfigDao();
        AlarmRuleService service = newService(ruleDao, localConfigDao);
        AlarmRuleV2 rule = validRule(null);
        rule.setName("checkout errors");
        rule.setDescription("watches the checkout page");

        service.createRule(rule);

        // insertRule writes no text at all, so a standalone rule that never reached the
        // local config would come back nameless and fail to resolve in the batch.
        assertEquals(List.of(), localConfigDao.deletedRuleIds);
        assertEquals(1, localConfigDao.upserted.size());
        AlarmRuleLocalConfig saved = localConfigDao.upserted.get(0);
        assertEquals(11L, saved.getRuleId());
        assertEquals("checkout errors", saved.getName());
        assertEquals("watches the checkout page", saved.getDescription());
        assertEquals(AlarmSeverity.CRITICAL, saved.getSeverity());
        assertEquals(60, saved.getCheckIntervalSec());
        assertNotNull(saved.getConditions());
    }

    @Test
    void createTemplateRuleWithTheItemsOwnTextStoresNoOverride() {
        RecordingLocalConfigDao localConfigDao = new RecordingLocalConfigDao();
        AlarmRuleService service = newBundleService(localConfigDao);
        AlarmRuleV2 rule = bundleRule("item");

        service.createRule(rule);

        // Same text as the item means inherit, not override: keeping a copy would freeze
        // the rule at today's name and make it ignore later item edits.
        assertEquals(List.of(), localConfigDao.upserted);
        assertEquals(List.of(11L), localConfigDao.deletedRuleIds);
    }

    @Test
    void createTemplateRuleKeepsANameThatDiffersFromTheItem() {
        RecordingLocalConfigDao localConfigDao = new RecordingLocalConfigDao();
        AlarmRuleService service = newBundleService(localConfigDao);
        AlarmRuleV2 rule = bundleRule("checkout errors");

        service.createRule(rule);

        assertEquals(List.of(), localConfigDao.deletedRuleIds);
        assertEquals(1, localConfigDao.upserted.size());
        AlarmRuleLocalConfig saved = localConfigDao.upserted.get(0);
        assertEquals("checkout errors", saved.getName());
        assertNull(saved.getDescription());
        assertNull(saved.getSeverity());
    }

    @Test
    void createTemplateRuleTreatsABlankNameAsInherited() {
        RecordingLocalConfigDao localConfigDao = new RecordingLocalConfigDao();
        AlarmRuleService service = newBundleService(localConfigDao);
        AlarmRuleV2 rule = bundleRule("   ");

        service.createRule(rule);

        // A blank override would render as an unnamed rule everywhere it is listed.
        assertEquals(List.of(), localConfigDao.upserted);
        assertEquals(List.of(11L), localConfigDao.deletedRuleIds);
    }

    @Test
    void createRuleUnwrapsSingleChildConditionGroups() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);
        AlarmCondition group = new AlarmCondition();
        group.setType(AlarmCondition.Type.GROUP);
        group.setOperator(AlarmCondition.Operator.AND);
        group.setCriteria(List.of(validCondition()));
        rule.setConditions(group);

        AlarmRuleV2 createdRule = service.createRule(rule);

        assertEquals(AlarmCondition.Type.LEAF, createdRule.getConditions().getType());
        assertEquals("sample_count", createdRule.getConditions().getMetric());
    }

    @Test
    void getRuleResponseUsesJoinedDetailsProjection() {
        AlarmRuleV2 rule = validRule(7L);
        rule.setSeverity(null);
        rule.setCheckIntervalSec(null);
        rule.setActionIntervalSec(null);
        rule.setConditions(null);

        AlarmRuleLocalConfig localConfig = new AlarmRuleLocalConfig();
        localConfig.setRuleId(7L);
        localConfig.setSeverity(AlarmSeverity.CRITICAL);
        localConfig.setCheckIntervalSec(60);
        localConfig.setActionIntervalSec(60);
        localConfig.setConditions(validCondition());
        localConfig.setFilters(List.of());

        AlarmRuleDetails details = new AlarmRuleDetails();
        details.setRule(rule);
        details.setLocalConfig(localConfig);

        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withRuleDetails(details);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmRuleResponse response = service.getRuleResponse(7L, APPLICATION);

        assertEquals(List.of(7L), ruleDao.selectedDetailsIds);
        assertEquals(AlarmSeverity.CRITICAL, response.getSeverity());
        assertEquals(localConfig, response.getLocalConfig());
    }

    @Test
    void getRuleResponseCarriesBundleContextForTemplateRule() {
        AlarmRuleV2 rule = validRule(7L);
        rule.setTemplateItemId(10L);
        rule.setSeverity(null);
        rule.setCheckIntervalSec(null);
        rule.setActionIntervalSec(null);
        rule.setConditions(null);

        AlarmRuleDetails details = new AlarmRuleDetails();
        details.setRule(rule);
        details.setTemplateItem(item(10L, 20L));
        details.setTemplate(template(20L, SERVICE_NAME));

        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withRuleDetails(details);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmRuleResponse response = service.getRuleResponse(7L, APPLICATION);

        assertEquals(10L, response.getTemplateItemId());
        assertEquals("item", response.getTemplateItemName());
        assertEquals(20L, response.getTemplateId());
        assertEquals("template", response.getTemplateName());
        assertEquals(details.getTemplateItem(), response.getTemplateItem());
    }

    @Test
    void createRuleRejectsUnknownApplication() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true, false);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createRule(rule)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(ruleDao.insertedRules.isEmpty());
    }

    // No checker claims the type, so nothing can say whether the application exists.
    @Test
    void createRuleRejectsAnApplicationTypeNoCheckerOwns() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao, new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);
        rule.setApplicationType("java");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createRule(rule)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("java"), exception.getReason());
        assertTrue(ruleDao.insertedRules.isEmpty());
    }

    @Test
    void createRuleRejectsMissingServiceName() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);
        rule.setServiceName(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createRule(rule)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("serviceName is required", exception.getReason());
        assertTrue(ruleDao.insertedRules.isEmpty());
    }

    @Test
    void createRuleRejectsTooLongServiceName() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);
        rule.setServiceName("a".repeat(AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH + 1));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createRule(rule)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("serviceName is too long", exception.getReason());
        assertTrue(ruleDao.insertedRules.isEmpty());
    }

    @Test
    void updateRuleLocksAndUpdatesOwnedRule() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);

        service.updateRule(7L, rule);

        assertEquals(List.of(7L), ruleDao.lockedIds);
        assertEquals(7L, rule.getId());
        assertEquals(SERVICE_NAME, rule.getServiceName());
        assertEquals(1, ruleDao.updatedRules.size());
        AlarmRuleV2 updatedRule = ruleDao.updatedRules.get(0);
        assertEquals(rule, updatedRule);
        assertEquals(7L, updatedRule.getId());
        assertEquals(AlarmApplication.TYPE_JAVASCRIPT, updatedRule.getApplicationType());
        assertEquals(SERVICE_NAME, updatedRule.getServiceName());
    }

    @Test
    void updateRuleRejectsDifferentDefaultServiceNameCase() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(7L);
        rule.setServiceName("default");

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.updateRule(7L, rule)
        );

        assertEquals("Rule not found", exception.getMessage());
        // Ownership is checked on the unlocked pre-read, so a rejected caller
        // never locks the rule row or any bundle header.
        assertTrue(ruleDao.lockedIds.isEmpty());
        assertTrue(ruleDao.updatedRules.isEmpty());
    }

    @Test
    void createTemplateLinkedRuleRejectsDifferentDefaultServiceNameCase() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withTargetApplicationServiceName("default");
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, "DEFAULT"));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        AlarmRuleService service = newService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);
        rule.setServiceName("default");
        rule.setTemplateItemId(10L);
        rule.setSeverity(null);
        rule.setCheckIntervalSec(null);
        rule.setActionIntervalSec(null);
        rule.setConditions(null);

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.createRule(rule)
        );

        assertEquals("Template not found", exception.getMessage());
        assertTrue(ruleDao.insertedRules.isEmpty());
        // Ownership is checked on the unlocked pre-read, so a rejected caller never
        // locks another service's bundle header -- same rule as updateRule above.
        assertTrue(templateDao.lockedIds.isEmpty());
    }

    // alarm_rule_v2.data_source is NOT NULL and the item is what governs a bundle rule, so
    // the request must not decide the value: resolveTemplateItemUnderLock stamps the item's
    // over whatever arrived, and an omitted one would otherwise reach the column as null.
    @Test
    void templateLinkedRuleStoresTheItemDataSourceNotTheRequestOne() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        AlarmRuleService service = newService(ruleDao, templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);
        rule.setTemplateItemId(10L);
        rule.setDataSource(null);
        rule.setSeverity(null);
        rule.setCheckIntervalSec(null);
        rule.setActionIntervalSec(null);
        rule.setConditions(null);

        service.createRule(rule);

        assertEquals(1, ruleDao.insertedRules.size());
        assertEquals(TestAlarmDataSource.AGENT_STAT.name(), ruleDao.insertedRules.get(0).getDataSource());
    }

    @Test
    void updateRuleLocksCurrentAndNewBundleHeadersInIdOrder() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withTemplateItemId(21L);
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(100L, SERVICE_NAME));
        templateDao.addTemplate(template(200L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(11L, 100L));
        templateItemDao.addItem(item(21L, 200L));
        AlarmRuleService service = newService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(null);
        rule.setTemplateItemId(11L);
        rule.setSeverity(null);
        rule.setCheckIntervalSec(null);
        rule.setActionIntervalSec(null);
        rule.setConditions(null);

        service.updateRule(7L, rule);

        assertEquals(List.of(100L, 200L), templateDao.lockedIds);
        assertEquals(List.of(rule), ruleDao.updatedRules);
    }

    @Test
    void ruleMutationsLockBundleHeaderBeforeTheRuleRow() {
        // Bundle edits lock the header first and then the rules; rule edits must use the
        // same order or the two deadlock each other.
        List<String> lockLog = new ArrayList<>();
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withTemplateItemId(10L).withLockLog(lockLog);
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME)).withLockLog(lockLog);
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        AlarmRuleService service = newService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        service.updateEnabled(SERVICE_NAME, APPLICATION_NAME, 7L, true);

        assertEquals(List.of("header:20", "rule:7"), lockLog);
    }

    @Test
    void updateRuleRejectsRuleOwnedByDifferentApplication() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(7L);
        rule.setApplicationName("other-app");

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.updateRule(7L, rule)
        );

        assertEquals("Rule not found", exception.getMessage());
        // Ownership is checked on the unlocked pre-read, so a rejected caller
        // never locks the rule row or any bundle header.
        assertTrue(ruleDao.lockedIds.isEmpty());
        assertTrue(ruleDao.updatedRules.isEmpty());
    }

    @Test
    void updateEnabledLocksAndUpdatesOwnedRule() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        service.updateEnabled(SERVICE_NAME, APPLICATION_NAME, 7L, false);

        assertEquals(List.of(7L), ruleDao.lockedIds);
        assertEquals(List.of(7L), ruleDao.updatedEnabledIds);
        assertEquals(List.of(false), ruleDao.updatedEnabledValues);
    }

    @Test
    void updateEnabledLocksReferencedBundleHeader() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withTemplateItemId(10L);
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        AlarmRuleService service = newService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        service.updateEnabled(SERVICE_NAME, APPLICATION_NAME, 7L, true);

        assertEquals(List.of(7L), ruleDao.lockedIds);
        assertEquals(List.of(20L), templateDao.lockedIds);
        assertEquals(List.of(7L), ruleDao.updatedEnabledIds);
    }

    @Test
    void updateEnabledRejectsRuleWhoseBundleHeaderBelongsToAnotherService() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withTemplateItemId(10L);
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, "other-service"));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        AlarmRuleService service = newService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.updateEnabled(SERVICE_NAME, APPLICATION_NAME, 7L, true)
        );

        assertEquals("Template not found", exception.getMessage());
        // Rejected before any lock, so neither the header nor the rule row is held.
        assertTrue(templateDao.lockedIds.isEmpty());
        assertTrue(ruleDao.lockedIds.isEmpty());
        assertTrue(ruleDao.updatedEnabledIds.isEmpty());
    }

    @Test
    void updateRuleRejectsMoveIntoAnotherServicesBundle() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, "other-service"));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        AlarmRuleService service = newService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmRuleV2 rule = validRule(7L);
        rule.setTemplateItemId(10L);

        AlarmResourceNotFoundException exception = assertThrows(
                AlarmResourceNotFoundException.class,
                () -> service.updateRule(7L, rule)
        );

        assertEquals("Template not found", exception.getMessage());
        assertTrue(templateDao.lockedIds.isEmpty());
        assertTrue(ruleDao.lockedIds.isEmpty());
        assertTrue(ruleDao.updatedRules.isEmpty());
    }

    @Test
    void updateEnabledRejectsMissingRule() {
        RecordingRuleDao ruleDao = new RecordingRuleDao(false);
        AlarmRuleService service = newService(ruleDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateEnabled(SERVICE_NAME, APPLICATION_NAME, 7L, false)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Rule not found: 7", exception.getReason());
        // The missing rule is caught by the unlocked pre-read that decides which
        // bundle header to lock, so no row is ever locked.
        assertTrue(ruleDao.lockedIds.isEmpty());
        assertTrue(ruleDao.updatedEnabledIds.isEmpty());
    }

    @Test
    void deleteRuleDeletesHistoryMappingsAndStateBeforeRule() {
        // A rule's dependants must be deleted in one order everywhere, or this path and
        // the cleanup batch grab the same rows in opposite orders and deadlock.
        List<String> deletionLog = new ArrayList<>();
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withDeletionLog(deletionLog);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao()
                .withDeletionLog(deletionLog);
        AlarmHistoryV2Dao historyDao = mock(AlarmHistoryV2Dao.class);
        AlarmNotificationOutboxDao outboxDao = mock(AlarmNotificationOutboxDao.class);
        AlarmStateDao stateDao = mock(AlarmStateDao.class);
        AlarmRuleLocalConfigDao localConfigDao = mock(AlarmRuleLocalConfigDao.class);
        doAnswer(log(deletionLog, "outbox")).when(outboxDao).deleteByRuleId(7L);
        doAnswer(log(deletionLog, "history")).when(historyDao).deleteByRuleId(7L);
        doAnswer(log(deletionLog, "localConfig")).when(localConfigDao).delete(7L);
        doAnswer(log(deletionLog, "state")).when(stateDao).deleteByRuleId(7L);
        AlarmRuleService service = newService(ruleDao, localConfigDao, stub(AlarmTemplateDao.class),
                stub(AlarmTemplateItemDao.class), channelBindingDao,
                historyDao, outboxDao, stateDao);

        service.deleteRule(SERVICE_NAME, APPLICATION_NAME, 7L);

        assertEquals(List.of(7L), ruleDao.lockedIds);
        assertEquals(DELETION_ORDER, deletionLog);
        assertEquals(List.of(7L), ruleDao.deletedIds);
    }

    @Test
    void deleteRulesByApplicationDeletesLinkedRulesOnly() {
        // A rule's dependants must be deleted in one order everywhere, or this path and
        // the cleanup batch grab the same rows in opposite orders and deadlock.
        List<String> deletionLog = new ArrayList<>();
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withDeletionLog(deletionLog);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao()
                .withDeletionLog(deletionLog);
        AlarmHistoryV2Dao historyDao = mock(AlarmHistoryV2Dao.class);
        AlarmNotificationOutboxDao outboxDao = mock(AlarmNotificationOutboxDao.class);
        AlarmStateDao stateDao = mock(AlarmStateDao.class);
        AlarmRuleLocalConfigDao localConfigDao = mock(AlarmRuleLocalConfigDao.class);
        doAnswer(log(deletionLog, "outbox")).when(outboxDao).deleteByRuleIds(APPLICATION_RULE_IDS);
        doAnswer(log(deletionLog, "history")).when(historyDao).deleteByRuleIds(APPLICATION_RULE_IDS);
        doAnswer(log(deletionLog, "localConfig")).when(localConfigDao).deleteByRuleIds(APPLICATION_RULE_IDS);
        doAnswer(log(deletionLog, "state")).when(stateDao).deleteByRuleIds(APPLICATION_RULE_IDS);
        AlarmRuleService service = newService(ruleDao, localConfigDao, stub(AlarmTemplateDao.class),
                stub(AlarmTemplateItemDao.class), channelBindingDao,
                historyDao, outboxDao, stateDao);

        service.deleteRulesByApplication(APPLICATION);

        assertEquals(DELETION_ORDER, deletionLog);
        assertEquals(List.of(APPLICATION_NAME), ruleDao.resolvedApplications);
        // Every child delete is driven by the resolved ids, not by the unindexed
        // application columns -- that is what keeps the lock off the whole table.
        assertEquals(APPLICATION_RULE_IDS, ruleDao.deletedIds);
        assertEquals(APPLICATION_RULE_IDS, channelBindingDao.deletedOwnerIds);
    }

    @Test
    void intervalOptionsAreAscendingSoRoundUpNeverPicksASmallerValue() {
        assertEquals(
                AlarmValidationConstants.CHECK_INTERVAL_SEC_OPTIONS.stream().sorted().toList(),
                AlarmValidationConstants.CHECK_INTERVAL_SEC_OPTIONS);
        assertEquals(
                AlarmValidationConstants.ACTION_INTERVAL_SEC_OPTIONS.stream().sorted().toList(),
                AlarmValidationConstants.ACTION_INTERVAL_SEC_OPTIONS);
    }

    /** A bundle rule that inherits everything except the name under test. */
    private AlarmRuleV2 bundleRule(String name) {
        AlarmRuleV2 rule = validRule(null);
        rule.setTemplateItemId(10L);
        rule.setName(name);
        rule.setDescription(null);
        rule.setSeverity(null);
        rule.setCheckIntervalSec(null);
        rule.setActionIntervalSec(null);
        rule.setConditions(null);
        return rule;
    }

    private AlarmRuleService newBundleService(AlarmRuleLocalConfigDao localConfigDao) {
        return newService(new RecordingRuleDao(true), new RecordingTemplateDao(template(20L, SERVICE_NAME)),
                new RecordingTemplateItemDao(item(10L, 20L)), localConfigDao);
    }

    private AlarmRuleService newService(RecordingRuleDao ruleDao, AlarmRuleLocalConfigDao localConfigDao) {
        return newService(ruleDao, stub(AlarmTemplateDao.class), stub(AlarmTemplateItemDao.class),
                localConfigDao);
    }

    private AlarmRuleService newService(RecordingRuleDao ruleDao,
                                       AlarmTemplateDao templateDao,
                                       AlarmTemplateItemDao templateItemDao,
                                       AlarmRuleLocalConfigDao localConfigDao) {
        AlarmStateDao stateDao = mock(AlarmStateDao.class);
        AlarmChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmHistoryV2Dao historyDao = stub(AlarmHistoryV2Dao.class);
        return new AlarmRuleService(
                ruleDao,
                localConfigDao,
                templateDao,
                templateItemDao,
                channelBindingDao,
                historyDao,
                stateDao,
                new EffectiveAlarmRuleResolver(),
                new AlarmApplicationResolver(List.of(existenceChecker(ruleDao))),
                new AlarmBundleLocks(ruleDao, templateDao, templateItemDao),
                new AlarmConfigValidator(templateItemDao, new ConditionValidator(), new FilterKeyValidator(),
                        DATA_SOURCE_REGISTRY),
                new AlarmRuleStamper(ruleDao, stateDao),
                new AlarmRuleDeleter(ruleDao, localConfigDao, channelBindingDao, historyDao,
                        mock(AlarmNotificationOutboxDao.class), stateDao)
        );
    }

    private AlarmRuleService newService(RecordingRuleDao ruleDao,
                                       AlarmChannelBindingDao channelBindingDao,
                                       AlarmStateDao stateDao) {
        return newService(ruleDao, channelBindingDao,
                stub(AlarmHistoryV2Dao.class), stateDao);
    }

    private AlarmRuleService newService(RecordingRuleDao ruleDao,
                                       AlarmChannelBindingDao channelBindingDao,
                                       AlarmHistoryV2Dao historyDao,
                                       AlarmStateDao stateDao) {
        return newService(ruleDao, stub(AlarmTemplateDao.class), stub(AlarmTemplateItemDao.class),
                channelBindingDao, historyDao, stateDao);
    }

    private AlarmRuleService newService(RecordingRuleDao ruleDao,
                                       AlarmTemplateDao templateDao,
                                       AlarmTemplateItemDao templateItemDao,
                                       AlarmChannelBindingDao channelBindingDao,
                                       AlarmStateDao stateDao) {
        return newService(ruleDao, templateDao, templateItemDao, channelBindingDao,
                stub(AlarmHistoryV2Dao.class), stateDao);
    }

    private AlarmRuleService newService(RecordingRuleDao ruleDao,
                                       AlarmTemplateDao templateDao,
                                       AlarmTemplateItemDao templateItemDao,
                                       AlarmChannelBindingDao channelBindingDao,
                                       AlarmHistoryV2Dao historyDao,
                                       AlarmStateDao stateDao) {
        return newService(ruleDao, templateDao, templateItemDao, channelBindingDao,
                historyDao, mock(AlarmNotificationOutboxDao.class), stateDao);
    }

    private AlarmRuleService newService(RecordingRuleDao ruleDao,
                                       AlarmTemplateDao templateDao,
                                       AlarmTemplateItemDao templateItemDao,
                                       AlarmChannelBindingDao channelBindingDao,
                                       AlarmHistoryV2Dao historyDao,
                                       AlarmNotificationOutboxDao outboxDao,
                                       AlarmStateDao stateDao) {
        return newService(ruleDao, mock(AlarmRuleLocalConfigDao.class), templateDao, templateItemDao,
                channelBindingDao, historyDao, outboxDao, stateDao);
    }

    private AlarmRuleService newService(RecordingRuleDao ruleDao,
                                       AlarmRuleLocalConfigDao localConfigDao,
                                       AlarmTemplateDao templateDao,
                                       AlarmTemplateItemDao templateItemDao,
                                       AlarmChannelBindingDao channelBindingDao,
                                       AlarmHistoryV2Dao historyDao,
                                       AlarmNotificationOutboxDao outboxDao,
                                       AlarmStateDao stateDao) {
        return new AlarmRuleService(
                ruleDao,
                localConfigDao,
                templateDao,
                templateItemDao,
                channelBindingDao,
                historyDao,
                stateDao,
                new EffectiveAlarmRuleResolver(),
                new AlarmApplicationResolver(List.of(existenceChecker(ruleDao))),
                new AlarmBundleLocks(ruleDao, templateDao, templateItemDao),
                new AlarmConfigValidator(templateItemDao, new ConditionValidator(), new FilterKeyValidator(),
                        DATA_SOURCE_REGISTRY),
                new AlarmRuleStamper(ruleDao, stateDao),
                new AlarmRuleDeleter(ruleDao, localConfigDao, channelBindingDao, historyDao,
                        outboxDao, stateDao)
        );
    }
}
