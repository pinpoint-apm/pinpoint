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
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AlarmTemplateServiceTest extends AlarmServiceTestSupport {

    // ---- Template bundle CRUD ----

    @Test
    void createTemplateInsertsHeaderAndItems() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        template.setItems(List.of(itemRequest(), itemRequest()));

        AlarmTemplate created = service.createTemplate(template);

        assertEquals(1, templateDao.insertedCount);
        assertEquals(2, templateItemDao.insertedItems.size());
        assertTrue(templateItemDao.insertedItems.stream()
                .allMatch(item -> Objects.equals(created.getId(), item.getTemplateId())));
    }

    @Test
    void createTemplateMintsNewItemIdsEvenWhenCopySourceIdsArePresent() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        AlarmTemplateItem copied = itemRequest();
        copied.setId(999L);
        template.setItems(List.of(copied));

        service.createTemplate(template);

        assertEquals(1, templateItemDao.insertedItems.size());
        assertTrue(templateItemDao.insertedItems.get(0).getId() < 999L);
    }

    @Test
    void updateTemplateStampsAnAddedItemOntoApplicationsAlreadyUsingTheBundle() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        // Two applications already carry a rule stamped from item 10.
        RecordingRuleDao ruleDao = new RecordingRuleDao(true)
                .withAppliedBundleRule(20L, APPLICATION_NAME, 55L)
                .withAppliedBundleRule(20L, "other-app", 56L);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmTemplate template = template(20L, SERVICE_NAME);
        AlarmTemplateItem added = item(null, 20L);
        added.setName("Affected users");
        template.setItems(List.of(item(10L, 20L), added));

        service.updateTemplate(template);

        assertEquals(2, ruleDao.insertedRules.size());
        assertEquals(List.of("other-app", APPLICATION_NAME),
                ruleDao.insertedRules.stream().map(AlarmRuleV2::getApplicationName).toList());
        // Config is inherited, so a stamped rule is only a link to the new item.
        assertTrue(ruleDao.insertedRules.stream().allMatch(rule -> rule.getName() == null));
        assertTrue(ruleDao.insertedRules.stream().allMatch(AlarmRuleV2::isEnabled));
    }

    @Test
    void updateTemplateStampsNothingWhenTheBundleIsNotAppliedAnywhere() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmTemplate template = template(20L, SERVICE_NAME);
        template.setItems(List.of(item(10L, 20L), item(null, 20L)));

        service.updateTemplate(template);

        assertTrue(ruleDao.insertedRules.isEmpty());
    }

    @Test
    void updateTemplateDiffsItemsAndSoftDeletesRemovedItems() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        templateItemDao.addItem(item(11L, 20L));
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmHistoryV2Dao.class), mock(AlarmStateDao.class));

        AlarmTemplate update = template(20L, SERVICE_NAME);
        AlarmTemplateItem updatedItem = itemRequest();
        updatedItem.setId(10L);
        updatedItem.setName("renamed-item");
        AlarmTemplateItem newItem = itemRequest();
        update.setItems(List.of(updatedItem, newItem));

        service.updateTemplate(update);

        assertEquals(List.of(20L), templateDao.lockedIds);
        assertEquals(1, templateDao.updatedCount);
        assertEquals(List.of(10L), templateItemDao.updatedItemIds);
        assertEquals(1, templateItemDao.insertedItems.size());
        assertEquals(List.of(11L), templateItemDao.markedDeletedIds);
        // Removed items are soft-deleted; their rules are hidden at once and purged by
        // the cleanup batch, so the request transaction deletes nothing itself.
        assertTrue(ruleDao.deletedIds.isEmpty());
    }

    @Test
    void updateTemplateRejectsItemOfAnotherTemplate() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmTemplate update = template(20L, SERVICE_NAME);
        AlarmTemplateItem foreignItem = itemRequest();
        foreignItem.setId(99L);
        update.setItems(List.of(foreignItem));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateTemplate(update));

        assertEquals("Template item does not belong to the template: itemId=99", exception.getMessage());
        assertEquals(0, templateDao.updatedCount);
        assertTrue(templateItemDao.updatedItemIds.isEmpty());
    }

    // Answering "no templates" would read as an empty service rather than a bad code.
    @Test
    void getTemplatesRejectsADataSourceNoInstalledModuleOwns() {
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true),
                new RecordingTemplateDao(template(20L, SERVICE_NAME)),
                new RecordingTemplateItemDao(item(10L, 20L)),
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        assertThrows(IllegalArgumentException.class,
                () -> service.getTemplates(SERVICE_NAME, "NOT_INSTALLED"));
    }

    @Test
    void updateTemplateRejectsItemDataSourceChangeWithLinkedRules() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        templateItemDao.ruleCounts.put(10L, 1);
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmTemplate update = template(20L, SERVICE_NAME);
        AlarmTemplateItem changedItem = itemRequest();
        changedItem.setId(10L);
        changedItem.setDataSource(TestAlarmDataSource.APPLICATION_RESPONSE.name());
        changedItem.setConditions(validSessionCondition());
        update.setItems(List.of(changedItem));

        AlarmResourceConflictException exception = assertThrows(
                AlarmResourceConflictException.class,
                () -> service.updateTemplate(update));

        assertEquals(
                "Template item dataSource cannot be changed while rules reference it: itemId=10",
                exception.getMessage());
        assertEquals(0, templateDao.updatedCount);
        assertTrue(templateItemDao.updatedItemIds.isEmpty());
    }

    // The stored code and the request's carry the same characters in different String
    // instances, which is what an identity comparison would reject as a change.
    @Test
    void updateTemplateAllowsAnUnchangedItemDataSourceWithLinkedRules() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        templateItemDao.ruleCounts.put(10L, 1);
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmTemplate update = template(20L, SERVICE_NAME);
        AlarmTemplateItem unchangedItem = itemRequest();
        unchangedItem.setId(10L);
        unchangedItem.setDataSource(new String(TestAlarmDataSource.AGENT_STAT.name()));
        update.setItems(List.of(unchangedItem));

        service.updateTemplate(update);

        assertEquals(1, templateDao.updatedCount);
        assertEquals(List.of(10L), templateItemDao.updatedItemIds);
    }

    @Test
    void updateTemplateAllowsItemDataSourceChangeWithoutLinkedRules() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmTemplate update = template(20L, SERVICE_NAME);
        AlarmTemplateItem changedItem = itemRequest();
        changedItem.setId(10L);
        changedItem.setDataSource(TestAlarmDataSource.APPLICATION_RESPONSE.name());
        changedItem.setConditions(validSessionCondition());
        update.setItems(List.of(changedItem));

        service.updateTemplate(update);

        assertEquals(List.of(20L), templateDao.lockedIds);
        assertEquals(1, templateDao.updatedCount);
        assertEquals(List.of(10L), templateItemDao.updatedItemIds);
    }

    @Test
    void deleteTemplateMarksHeaderAndItemsDeletedWithoutDeletingLinkedData() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withTemplateItemId(10L);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmHistoryV2Dao historyDao = mock(AlarmHistoryV2Dao.class);
        AlarmNotificationOutboxDao outboxDao = mock(AlarmNotificationOutboxDao.class);
        AlarmStateDao stateDao = mock(AlarmStateDao.class);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,                 channelBindingDao, historyDao, outboxDao, stateDao);

        service.deleteTemplate(SERVICE_NAME, 20L);

        assertEquals(List.of(20L), templateDao.lockedIds);
        assertEquals(List.of(20L), templateItemDao.markedDeletedTemplateIds);
        assertTrue(ruleDao.deletedIds.isEmpty());
        assertTrue(channelBindingDao.deletedOwnerIds.isEmpty());
        verifyNoInteractions(outboxDao, historyDao, stateDao);
        assertEquals(1, templateDao.markedDeletedCount);
    }

    @Test
    void createTemplateRejectsInvalidCheckInterval() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        AlarmTemplateItem item = itemRequest();
        item.setCheckIntervalSec(10); // < 60
        template.setItems(List.of(item));

        assertThrows(IllegalArgumentException.class, () -> service.createTemplate(template));
        assertEquals(0, templateDao.insertedCount);
        assertTrue(templateItemDao.insertedItems.isEmpty());
    }

    @Test
    void createTemplateRoundsUpItemIntervalsToSelectableOptions() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        AlarmTemplateItem item = itemRequest();
        item.setCheckIntervalSec(120);
        item.setActionIntervalSec(400);
        template.setItems(List.of(item));

        service.createTemplate(template);

        AlarmTemplateItem saved = templateItemDao.insertedItems.get(0);
        assertEquals(300, saved.getCheckIntervalSec());
        assertEquals(600, saved.getActionIntervalSec());
        assertEquals(1, templateDao.insertedCount);
    }

    @Test
    void createTemplateUnwrapsSingleChildItemConditionGroups() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        AlarmTemplateItem item = itemRequest();
        AlarmCondition group = new AlarmCondition();
        group.setType(AlarmCondition.Type.GROUP);
        group.setOperator(AlarmCondition.Operator.OR);
        group.setCriteria(List.of(validCondition()));
        item.setConditions(group);
        template.setItems(List.of(item));

        service.createTemplate(template);

        AlarmTemplateItem saved = templateItemDao.insertedItems.get(0);
        assertEquals(AlarmCondition.Type.LEAF, saved.getConditions().getType());
    }

    @Test
    void createTemplateRejectsIntervalAboveLargestSelectableOption() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        AlarmTemplateItem item = itemRequest();
        item.setCheckIntervalSec(7200); // > 1h
        template.setItems(List.of(item));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createTemplate(template)
        );

        assertEquals("checkIntervalSec must be at most 3600", exception.getMessage());
        assertEquals(0, templateDao.insertedCount);
    }

    @Test
    void createTemplateRejectsNullItemConditions() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        AlarmTemplateItem item = itemRequest();
        item.setConditions(null);
        template.setItems(List.of(item));

        assertThrows(IllegalArgumentException.class, () -> service.createTemplate(template));
        assertEquals(0, templateDao.insertedCount);
    }

    @Test
    void createTemplateRejectsNullItemDataSource() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        AlarmTemplateItem item = itemRequest();
        item.setDataSource(null);
        template.setItems(List.of(item));

        assertThrows(IllegalArgumentException.class, () -> service.createTemplate(template));
        assertEquals(0, templateDao.insertedCount);
    }

    @Test
    void createTemplateRejectsEmptyItems() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao();
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao();
        AlarmTemplateService service = newTemplateService(new RecordingRuleDao(true), templateDao, templateItemDao,
                new RecordingChannelBindingDao(), mock(AlarmStateDao.class));
        AlarmTemplate template = template(null, SERVICE_NAME);
        template.setItems(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.createTemplate(template));
        assertEquals(0, templateDao.insertedCount);
    }

    // ---- Template apply ----

    @Test
    void applyTemplateCreatesOneRulePerItem() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        templateItemDao.addItem(item(11L, 20L));
        RecordingRuleDao ruleDao = new RecordingRuleDao(true);
        AlarmStateDao stateDao = mock(AlarmStateDao.class);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), stateDao);

        List<AlarmRuleV2> created = service.applyTemplate(SERVICE_NAME, APPLICATION_NAME, 20L, AlarmApplication.TYPE_JAVASCRIPT);

        assertEquals(List.of(20L), templateDao.lockedIds);
        assertEquals(2, created.size());
        assertEquals(List.of(10L, 11L), created.stream().map(AlarmRuleV2::getTemplateItemId).toList());
        assertEquals(2, ruleDao.insertedRules.size());
        assertTrue(created.stream().allMatch(rule -> rule.getApplicationType().equals(AlarmApplication.TYPE_JAVASCRIPT)));
        assertTrue(created.stream().allMatch(AlarmRuleV2::isEnabled));
        // Name, description and conditions are all left unset so the rule follows its
        // item; copying them here would freeze values later item edits cannot reach.
        assertNull(created.get(0).getName());
        assertNull(created.get(0).getDescription());
        assertNull(created.get(0).getConditions());
    }

    @Test
    void applyTemplateRejectsSecondApplyToSameApplication() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withAppliedBundleRule(20L, APPLICATION_NAME, 55L);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        AlarmResourceConflictException exception = assertThrows(
                AlarmResourceConflictException.class,
                () -> service.applyTemplate(SERVICE_NAME, APPLICATION_NAME, 20L, AlarmApplication.TYPE_JAVASCRIPT));

        assertEquals("Template is already applied to the application: templateId=20, application="
                + new AlarmApplication(SERVICE_NAME, APPLICATION_NAME, AlarmApplication.TYPE_JAVASCRIPT),
                exception.getMessage());
        assertTrue(ruleDao.insertedRules.isEmpty());
    }

    @Test
    void applyTemplateRejectsUnknownApplication() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        RecordingRuleDao ruleDao = new RecordingRuleDao(true, false);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.applyTemplate(SERVICE_NAME, APPLICATION_NAME, 20L, AlarmApplication.TYPE_JAVASCRIPT));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(ruleDao.insertedRules.isEmpty());
    }

    @Test
    void unapplyTemplateDeletesBundleRulesOfApplication() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        RecordingRuleDao ruleDao = new RecordingRuleDao(true)
                .withAppliedBundleRule(20L, APPLICATION_NAME, 55L)
                .withAppliedBundleRule(20L, APPLICATION_NAME, 56L);
        RecordingChannelBindingDao channelBindingDao = new RecordingChannelBindingDao();
        AlarmHistoryV2Dao historyDao = mock(AlarmHistoryV2Dao.class);
        AlarmNotificationOutboxDao outboxDao = mock(AlarmNotificationOutboxDao.class);
        AlarmStateDao stateDao = mock(AlarmStateDao.class);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,                 channelBindingDao, historyDao, outboxDao, stateDao);

        service.unapplyTemplate(SERVICE_NAME, APPLICATION_NAME, 20L, AlarmApplication.TYPE_JAVASCRIPT);

        assertEquals(List.of(20L), templateDao.lockedIds);
        assertEquals(List.of(55L, 56L), ruleDao.deletedIds);
        verify(outboxDao).deleteByRuleId(55L);
        verify(historyDao).deleteByRuleId(55L);
        verify(stateDao).deleteByRuleId(56L);
    }

    @Test
    void unapplyTemplateRejectsApplicationOfAnotherApplication() {
        RecordingTemplateDao templateDao = new RecordingTemplateDao(template(20L, SERVICE_NAME));
        RecordingTemplateItemDao templateItemDao = new RecordingTemplateItemDao(item(10L, 20L));
        RecordingRuleDao ruleDao = new RecordingRuleDao(true).withAppliedBundleRule(20L, APPLICATION_NAME, 55L);
        AlarmTemplateService service = newTemplateService(ruleDao, templateDao, templateItemDao,                 new RecordingChannelBindingDao(), mock(AlarmStateDao.class));

        // The permission check ran against "other-app", so tearing down the rules of a
        // different application must be refused before anything is deleted.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.unapplyTemplate(SERVICE_NAME, "other-app", 20L,
                        AlarmApplication.TYPE_JAVASCRIPT));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(ruleDao.deletedIds.isEmpty());
    }

    private AlarmTemplateService newTemplateService(RecordingRuleDao ruleDao,
                                                    AlarmTemplateDao templateDao,
                                                    AlarmTemplateItemDao templateItemDao,
                                                    AlarmChannelBindingDao channelBindingDao,
                                                    AlarmStateDao stateDao) {
        return newTemplateService(ruleDao, templateDao, templateItemDao, channelBindingDao,
                stub(AlarmHistoryV2Dao.class), mock(AlarmNotificationOutboxDao.class), stateDao);
    }

    private AlarmTemplateService newTemplateService(RecordingRuleDao ruleDao,
                                                    AlarmTemplateDao templateDao,
                                                    AlarmTemplateItemDao templateItemDao,
                                                    AlarmChannelBindingDao channelBindingDao,
                                                    AlarmHistoryV2Dao historyDao,
                                                    AlarmStateDao stateDao) {
        return newTemplateService(ruleDao, templateDao, templateItemDao, channelBindingDao,
                historyDao, mock(AlarmNotificationOutboxDao.class), stateDao);
    }

    private AlarmTemplateService newTemplateService(RecordingRuleDao ruleDao,
                                                    AlarmTemplateDao templateDao,
                                                    AlarmTemplateItemDao templateItemDao,
                                                    AlarmChannelBindingDao channelBindingDao,
                                                    AlarmHistoryV2Dao historyDao,
                                                    AlarmNotificationOutboxDao outboxDao,
                                                    AlarmStateDao stateDao) {
        return new AlarmTemplateService(
                ruleDao,
                templateDao,
                templateItemDao,
                new AlarmApplicationResolver(List.of(existenceChecker(ruleDao))),
                new AlarmBundleLocks(ruleDao, templateDao, templateItemDao),
                new AlarmConfigValidator(templateItemDao, new ConditionValidator(), new FilterKeyValidator(),
                        DATA_SOURCE_REGISTRY),
                new AlarmRuleStamper(ruleDao, stateDao),
                new AlarmRuleDeleter(ruleDao, mock(AlarmRuleLocalConfigDao.class), channelBindingDao,
                        historyDao, outboxDao, stateDao),
                DATA_SOURCE_REGISTRY
        );
    }
}
