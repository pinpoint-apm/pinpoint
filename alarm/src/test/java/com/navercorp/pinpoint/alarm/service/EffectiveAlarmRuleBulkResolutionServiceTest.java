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

import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EffectiveAlarmRuleBulkResolutionServiceTest {

    @Test
    void resolvesRulesWithBulkFetchedLocalConfigsAndTemplates() {
        FakeLocalConfigDao localConfigDao = new FakeLocalConfigDao();
        FakeTemplateItemDao templateItemDao = new FakeTemplateItemDao();
        FakeTemplateDao templateDao = new FakeTemplateDao();
        EffectiveAlarmRuleBulkResolutionService resolver = new EffectiveAlarmRuleBulkResolutionService(
                localConfigDao, templateItemDao, templateDao, new EffectiveAlarmRuleResolver());

        AlarmTemplate template = template(20L, "Default Template");
        templateDao.templates.put(template.getId(), template);
        AlarmTemplateItem item = item(10L, 20L, "Error spike", "error_count");
        templateItemDao.items.put(item.getId(), item);

        AlarmRuleV2 templateRule = rule(1L, 10L);
        AlarmRuleV2 standaloneRule = rule(2L, null);
        localConfigDao.configs.put(standaloneRule.getId(), localConfig(standaloneRule.getId(), "session_count"));

        EffectiveAlarmRuleBulkResolutionService.ResolveResult result =
                resolver.resolveWithFailures(List.of(templateRule, standaloneRule));
        List<AlarmRuleV2> effectiveRules = result.rules();

        assertEquals(List.of(1L, 2L), localConfigDao.selectedRuleIds);
        assertEquals(List.of(10L), templateItemDao.selectedIds);
        assertEquals(List.of(20L), templateDao.selectedIds);
        assertEquals(0, result.failures().size());
        assertEquals(20L, effectiveRules.get(0).getTemplateId());
        assertEquals("Default Template", effectiveRules.get(0).getTemplateName());
        assertEquals("Error spike", effectiveRules.get(0).getTemplateItemName());
        assertEquals("error_count", effectiveRules.get(0).getConditions().getMetric());
        assertEquals(AlarmSeverity.WARNING, effectiveRules.get(0).getSeverity());
        assertEquals("session_count", effectiveRules.get(1).getConditions().getMetric());
        assertEquals(AlarmSeverity.CRITICAL, effectiveRules.get(1).getSeverity());
    }

    @Test
    void resolveWithFailuresReturnsResolvedRulesAndFailuresSeparately() {
        FakeLocalConfigDao localConfigDao = new FakeLocalConfigDao();
        FakeTemplateItemDao templateItemDao = new FakeTemplateItemDao();
        FakeTemplateDao templateDao = new FakeTemplateDao();
        EffectiveAlarmRuleBulkResolutionService resolver = new EffectiveAlarmRuleBulkResolutionService(
                localConfigDao, templateItemDao, templateDao, new EffectiveAlarmRuleResolver());

        AlarmRuleV2 brokenTemplateRule = rule(1L, 10L);
        AlarmRuleV2 standaloneRule = rule(2L, null);
        localConfigDao.configs.put(standaloneRule.getId(), localConfig(standaloneRule.getId(), "session_count"));

        EffectiveAlarmRuleBulkResolutionService.ResolveResult result =
                resolver.resolveWithFailures(List.of(brokenTemplateRule, standaloneRule));

        assertEquals(1, result.rules().size());
        assertEquals(standaloneRule.getId(), result.rules().get(0).getId());
        assertEquals(1, result.failures().size());
        assertEquals(brokenTemplateRule, result.failures().get(0).rule());
        assertEquals("templateItem must not be null for template-linked rule: ruleId=1",
                result.failures().get(0).exception().getMessage());
    }

    private AlarmRuleV2 rule(Long id, Long templateItemId) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(id);
        rule.setName("rule-" + id);
        rule.setServiceName("service");
        rule.setApplicationName("app");
        rule.setDataSource(TestAlarmDataSource.PRIMARY.name());
        rule.setTemplateItemId(templateItemId);
        rule.setEnabled(true);
        return rule;
    }

    private AlarmTemplate template(Long id, String name) {
        AlarmTemplate template = new AlarmTemplate();
        template.setId(id);
        template.setServiceName("service");
        template.setName(name);
        return template;
    }

    private AlarmTemplateItem item(Long id, Long templateId, String name, String metric) {
        AlarmTemplateItem item = new AlarmTemplateItem();
        item.setId(id);
        item.setTemplateId(templateId);
        item.setName(name);
        item.setDataSource(TestAlarmDataSource.PRIMARY.name());
        item.setSeverity(AlarmSeverity.WARNING);
        item.setCheckIntervalSec(300);
        item.setActionIntervalSec(3600);
        item.setConditions(condition(metric));
        item.setFilters(List.of());
        return item;
    }

    private AlarmRuleLocalConfig localConfig(Long ruleId, String metric) {
        AlarmRuleLocalConfig localConfig = new AlarmRuleLocalConfig();
        localConfig.setRuleId(ruleId);
        localConfig.setSeverity(AlarmSeverity.CRITICAL);
        localConfig.setCheckIntervalSec(300);
        localConfig.setActionIntervalSec(3600);
        localConfig.setConditions(condition(metric));
        localConfig.setFilters(List.of());
        return localConfig;
    }

    private AlarmCondition condition(String metric) {
        AlarmCondition condition = new AlarmCondition();
        condition.setType(AlarmCondition.Type.LEAF);
        condition.setMetric(metric);
        condition.setOp(AlarmCondition.ComparisonOp.GTE);
        condition.setThreshold(1.0);
        condition.setWindowSec(300);
        condition.setAggregation(AlarmCondition.Aggregation.COUNT);
        return condition;
    }

    private static class FakeLocalConfigDao implements AlarmRuleLocalConfigDao {
        private final Map<Long, AlarmRuleLocalConfig> configs = new HashMap<>();
        private List<Long> selectedRuleIds = List.of();

        @Override
        public void upsert(AlarmRuleLocalConfig config) {
        }

        @Override
        public void delete(Long ruleId) {
        }

        @Override
        public void deleteByRuleIds(java.util.List<Long> ruleIds) {
        }

        @Override
        public AlarmRuleLocalConfig selectByRuleId(Long ruleId) {
            return configs.get(ruleId);
        }

        @Override
        public List<AlarmRuleLocalConfig> selectByRuleIds(List<Long> ruleIds) {
            selectedRuleIds = new ArrayList<>(ruleIds);
            return ruleIds.stream()
                    .map(configs::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
    }

    private static class FakeTemplateItemDao implements AlarmTemplateItemDao {
        private final Map<Long, AlarmTemplateItem> items = new HashMap<>();
        private List<Long> selectedIds = List.of();

        @Override
        public void insert(AlarmTemplateItem item) {
        }

        @Override
        public int update(AlarmTemplateItem item) {
            return 0;
        }

        @Override
        public int markDeleted(Long id) {
            return 0;
        }

        @Override
        public int markDeletedByTemplateId(Long templateId) {
            return 0;
        }

        @Override
        public AlarmTemplateItem selectById(Long id) {
            return items.get(id);
        }

        @Override
        public AlarmTemplateItem selectByIdForUpdate(Long id) {
            return items.get(id);
        }

        @Override
        public List<AlarmTemplateItem> selectByIds(List<Long> ids) {
            selectedIds = new ArrayList<>(ids);
            return ids.stream()
                    .map(items::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        @Override
        public List<AlarmTemplateItem> selectByTemplateId(Long templateId) {
            return List.of();
        }

        @Override
        public List<AlarmTemplateItem> selectByTemplateIds(List<Long> templateIds) {
            return List.of();
        }

        @Override
        public int countRulesByTemplateItemId(Long templateItemId) {
            return 0;
        }
    }

    private static class FakeTemplateDao implements AlarmTemplateDao {
        private final Map<Long, AlarmTemplate> templates = new HashMap<>();
        private List<Long> selectedIds = List.of();

        @Override
        public void insert(AlarmTemplate template) {
        }

        @Override
        public int update(AlarmTemplate template) {
            return 0;
        }

        @Override
        public int markDeleted(Long id) {
            return 0;
        }

        @Override
        public AlarmTemplate selectById(Long id) {
            return templates.get(id);
        }

        @Override
        public AlarmTemplate selectByIdForUpdate(Long id) {
            return templates.get(id);
        }

        @Override
        public List<AlarmTemplate> selectByIds(List<Long> ids) {
            selectedIds = new ArrayList<>(ids);
            return ids.stream()
                    .map(templates::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        @Override
        public List<AlarmTemplate> selectByService(String serviceName) {
            return List.of();
        }

        @Override
        public List<AlarmTemplate> selectByServiceAndDataSource(String serviceName,
                                                                String dataSource) {
            return List.of();
        }
    }
}
