package com.navercorp.pinpoint.alarm.vo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlarmRuleV2Test {

    @Test
    void getOverrideKeys_returnsEmptyForStandaloneRule() {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setOverrideSeverity(true);
        rule.setOverrideConditions(true);

        assertEquals(List.of(), rule.getOverrideKeys());
    }

    @Test
    void getOverrideKeys_returnsLocalOverrideKeysForTemplateRule() {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setTemplateItemId(1L);
        rule.setOverrideSeverity(true);
        rule.setOverrideConditions(true);
        rule.setOverrideFilters(true);

        assertEquals(List.of("severity", "conditions", "filters"), rule.getOverrideKeys());
    }
}
