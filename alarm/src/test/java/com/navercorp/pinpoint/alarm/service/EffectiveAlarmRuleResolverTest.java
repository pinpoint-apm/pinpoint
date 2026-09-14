package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectiveAlarmRuleResolverTest {

    private final EffectiveAlarmRuleResolver resolver = new EffectiveAlarmRuleResolver();

    @Test
    void standaloneRuleUsesLocalConfig() {
        AlarmRuleV2 rule = rule(null, TestAlarmDataSource.PRIMARY.name());
        AlarmRuleLocalConfig localConfig = localConfig();
        localConfig.setSeverity(AlarmSeverity.WARNING);
        localConfig.setCheckIntervalSec(120);
        localConfig.setActionIntervalSec(600);
        localConfig.setConditions(condition("error_count"));
        localConfig.setFilters(List.of(filter("environment")));

        AlarmRuleV2 effective = resolver.resolve(rule, null, null, localConfig);

        assertEquals(AlarmSeverity.WARNING, effective.getSeverity());
        assertEquals(TestAlarmDataSource.PRIMARY.name(), effective.getDataSource());
        assertEquals(120, effective.getCheckIntervalSec());
        assertEquals(600, effective.getActionIntervalSec());
        assertEquals("error_count", effective.getConditions().getMetric());
        assertEquals(1, effective.getFilters().size());
    }

    @Test
    void standaloneRuleRequiresCompleteLocalConfig() {
        AlarmRuleV2 rule = rule(null, TestAlarmDataSource.PRIMARY.name());

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(rule, null, null, null));
    }

    @Test
    void resolvedRulePreservesRuleMetadata() {
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 20, 8, 30);
        AlarmRuleV2 rule = rule(null, TestAlarmDataSource.PRIMARY.name());
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setUpdatedAt(updatedAt);
        AlarmRuleLocalConfig localConfig = localConfig();
        // A standalone rule's text lives in the local config with the rest of its config.
        localConfig.setName("rule");
        localConfig.setDescription("description");
        localConfig.setSeverity(AlarmSeverity.WARNING);
        localConfig.setCheckIntervalSec(120);
        localConfig.setActionIntervalSec(600);
        localConfig.setConditions(condition("error_count"));

        AlarmRuleV2 effective = resolver.resolve(rule, null, null, localConfig);

        assertEquals(rule.getId(), effective.getId());
        assertEquals(localConfig.getName(), effective.getName());
        assertEquals(localConfig.getDescription(), effective.getDescription());
        assertEquals(rule.getServiceName(), effective.getServiceName());
        assertEquals(rule.getApplicationName(), effective.getApplicationName());
        assertEquals(rule.getApplicationType(), effective.getApplicationType());
        assertEquals(rule.getTemplateId(), effective.getTemplateId());
        assertEquals(rule.isEnabled(), effective.isEnabled());
        assertEquals(rule.getUpdatedAt(), effective.getUpdatedAt());
    }

    @Test
    void templateLinkedRuleInheritsTemplateWhenLocalConfigIsMissing() {
        AlarmRuleV2 rule = rule(10L, TestAlarmDataSource.PRIMARY.name());
        AlarmTemplateItem item = item();
        AlarmTemplate template = template();

        AlarmRuleV2 effective = resolver.resolve(rule, item, template, null);

        assertEquals(item.getDataSource(), effective.getDataSource());
        assertEquals(item.getSeverity(), effective.getSeverity());
        assertEquals(item.getCheckIntervalSec(), effective.getCheckIntervalSec());
        assertEquals(item.getActionIntervalSec(), effective.getActionIntervalSec());
        assertEquals(item.getConditions(), effective.getConditions());
        assertEquals(item.getFilters(), effective.getFilters());
        assertEquals(template.getId(), effective.getTemplateId());
        assertEquals(template.getName(), effective.getTemplateName());
        assertEquals(item.getName(), effective.getTemplateItemName());
        assertTrue(effective.getOverrideKeys().isEmpty());
    }

    @Test
    void templateLinkedRuleOverridesOnlyNonNullFields() {
        AlarmRuleV2 rule = rule(10L, TestAlarmDataSource.PRIMARY.name());
        AlarmTemplateItem item = item();
        AlarmTemplate template = template();
        AlarmRuleLocalConfig localConfig = new AlarmRuleLocalConfig();
        localConfig.setRuleId(rule.getId());
        localConfig.setSeverity(AlarmSeverity.WARNING);
        localConfig.setConditions(condition("affected_user_count"));

        AlarmRuleV2 effective = resolver.resolve(rule, item, template, localConfig);

        assertEquals(AlarmSeverity.WARNING, effective.getSeverity());
        assertEquals(item.getCheckIntervalSec(), effective.getCheckIntervalSec());
        assertEquals(item.getActionIntervalSec(), effective.getActionIntervalSec());
        assertEquals("affected_user_count", effective.getConditions().getMetric());
        assertEquals(item.getFilters(), effective.getFilters());
        assertEquals(List.of("severity", "conditions"), effective.getOverrideKeys());
        // Untouched name still comes from the item.
        assertEquals(item.getName(), effective.getName());
    }

    @Test
    void templateLinkedRuleInheritsItemNameAndDescription() {
        AlarmRuleV2 rule = rule(10L, TestAlarmDataSource.PRIMARY.name());
        AlarmTemplateItem item = item();
        item.setDescription("item description");

        AlarmRuleV2 effective = resolver.resolve(rule, item, template(), null);

        assertEquals(item.getName(), effective.getName());
        assertEquals(item.getDescription(), effective.getDescription());
        assertTrue(effective.getOverrideKeys().isEmpty());
    }

    @Test
    void templateLinkedRuleKeepsItsOwnNameAsAnOverride() {
        AlarmRuleV2 rule = rule(10L, TestAlarmDataSource.PRIMARY.name());
        AlarmTemplateItem item = item();
        AlarmRuleLocalConfig localConfig = localConfig();
        localConfig.setName("renamed by the user");

        AlarmRuleV2 effective = resolver.resolve(rule, item, template(), localConfig);

        assertEquals("renamed by the user", effective.getName());
        assertEquals(List.of("name"), effective.getOverrideKeys());
    }

    @Test
    void standaloneRuleKeepsItsOwnNameWithoutBeingAnOverride() {
        AlarmRuleV2 rule = rule(null, TestAlarmDataSource.PRIMARY.name());
        AlarmRuleLocalConfig localConfig = localConfig();
        localConfig.setSeverity(AlarmSeverity.WARNING);
        localConfig.setCheckIntervalSec(300);
        localConfig.setActionIntervalSec(1800);
        localConfig.setConditions(condition("error_count"));
        localConfig.setName("rule");

        AlarmRuleV2 effective = resolver.resolve(rule, null, null, localConfig);

        assertEquals("rule", effective.getName());
        // Nothing to inherit from, so its own name is not a deviation from anything.
        assertTrue(effective.getOverrideKeys().isEmpty());
    }

    @Test
    void emptyFiltersOverrideTemplateFilters() {
        AlarmRuleV2 rule = rule(10L, TestAlarmDataSource.PRIMARY.name());
        AlarmTemplateItem item = item();
        AlarmTemplate template = template();
        AlarmRuleLocalConfig localConfig = new AlarmRuleLocalConfig();
        localConfig.setRuleId(rule.getId());
        localConfig.setFilters(List.of());

        AlarmRuleV2 effective = resolver.resolve(rule, item, template, localConfig);

        assertTrue(effective.getFilters().isEmpty());
    }

    private AlarmRuleV2 rule(Long templateItemId, String dataSource) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        // The rule row carries no text at all now: it lives in the local config.
        rule.setServiceName("service");
        rule.setApplicationName("app");
        rule.setDataSource(dataSource);
        rule.setTemplateItemId(templateItemId);
        rule.setEnabled(true);
        return rule;
    }

    private AlarmTemplateItem item() {
        AlarmTemplateItem item = new AlarmTemplateItem();
        item.setId(10L);
        item.setTemplateId(20L);
        item.setName("item");
        item.setSeverity(AlarmSeverity.CRITICAL);
        item.setDataSource(TestAlarmDataSource.SECONDARY.name());
        item.setCheckIntervalSec(300);
        item.setActionIntervalSec(3600);
        item.setConditions(condition("session_count"));
        item.setFilters(List.of(filter("environment")));
        return item;
    }

    private AlarmTemplate template() {
        AlarmTemplate template = new AlarmTemplate();
        template.setId(20L);
        template.setServiceName("service");
        template.setName("template");
        return template;
    }

    private AlarmRuleLocalConfig localConfig() {
        AlarmRuleLocalConfig localConfig = new AlarmRuleLocalConfig();
        localConfig.setRuleId(1L);
        return localConfig;
    }

    private AlarmCondition condition(String metric) {
        AlarmCondition condition = new AlarmCondition();
        condition.setType(AlarmCondition.Type.LEAF);
        condition.setMetric(metric);
        condition.setOp(AlarmCondition.ComparisonOp.GTE);
        condition.setThreshold(100.0);
        condition.setWindowSec(300);
        condition.setAggregation(AlarmCondition.Aggregation.COUNT);
        return condition;
    }

    private AlarmFilter filter(String key) {
        AlarmFilter filter = new AlarmFilter();
        filter.setKey(key);
        filter.setOp(AlarmFilter.Op.EQ);
        filter.setValue("production");
        return filter;
    }
}
