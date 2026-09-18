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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.validation.ConditionValidator;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.validation.FilterKeyValidator;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmTemplatePresetLoaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConditionValidator conditionValidator = new ConditionValidator();
    private final FilterKeyValidator filterKeyValidator = new FilterKeyValidator();

    private AlarmTemplatePresetLoader load(Resource... resources) {
        return new AlarmTemplatePresetLoader(objectMapper, conditionValidator, filterKeyValidator,
                new AlarmDataSourceRegistry(List.of(new TestAlarmDataSource.Provider())), List.of(resources));
    }

    private static Resource resourceOf(String json) {
        return new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void loadsAndValidatesCatalog() {
        Resource resource = resourceOf("""
                {"name": {"ko": "프리셋", "en": "preset"},
                 "description": {"ko": "설명", "en": "description"},
                 "rules": [{
                  "name": {"ko": "규칙", "en": "rule"},
                  "description": {"ko": "규칙 설명", "en": "rule description"},
                  "severity": "WARNING", "dataSource": "AGENT_STAT",
                  "checkIntervalSec": 300, "actionIntervalSec": 1800,
                  "conditions": {"type": "LEAF", "metric": "deadlock_count", "trigger": "NEW_GROUP"}
                }]}
                """);

        List<AlarmTemplatePreset> presets = load(resource).getPresets();

        assertEquals(1, presets.size());
        AlarmTemplatePreset preset = presets.get(0);
        AlarmTemplatePreset.Rule newGroupRule = preset.rules().get(0);
        assertEquals(AlarmCondition.Trigger.NEW_GROUP, newGroupRule.conditions().getTrigger());
        assertNull(newGroupRule.conditions().getThreshold());
        assertTrue(preset.name().ko() != null && preset.name().en() != null
                && preset.description().ko() != null && preset.description().en() != null);
        // A rule explains itself in both locales; the rule name alone is what a
        // notification shows, so the description carries the reasoning.
        assertTrue(newGroupRule.description().ko() != null && newGroupRule.description().en() != null);
    }

    // A preset description is optional, the same way a rule's is, but one translated
    // halfway shows a blank in whichever locale it is missing.
    @Test
    void rejectsPresetDescriptionMissingOneLocale() {
        Resource resource = resourceOf("""
                {"name": {"ko": "프리셋", "en": "preset"},
                 "description": {"ko": "설명만 한국어"},
                 "rules": [{
                  "name": {"ko": "규칙", "en": "rule"},
                  "severity": "WARNING", "dataSource": "AGENT_STAT",
                  "checkIntervalSec": 300, "actionIntervalSec": 1800,
                  "conditions": {"type": "LEAF", "metric": "sample_count", "op": ">=", "threshold": 1,
                                 "windowSec": 300, "aggregation": "COUNT"}
                }]}
                """);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> load(resource));
        assertTrue(e.getMessage().contains("preset description"), e.getMessage());
    }

    @Test
    void rejectsRuleDescriptionMissingOneLocale() {
        Resource resource = resourceOf("""
                {"name": {"ko": "프리셋", "en": "preset"}, "rules": [{
                  "name": {"ko": "규칙", "en": "rule"},
                  "description": {"ko": "설명만 한국어"},
                  "severity": "WARNING", "dataSource": "AGENT_STAT",
                  "checkIntervalSec": 300, "actionIntervalSec": 1800,
                  "conditions": {"type": "LEAF", "metric": "sample_count", "op": ">=", "threshold": 1,
                                 "windowSec": 300, "aggregation": "COUNT"}
                }]}
                """);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> load(resource));
        assertTrue(e.getMessage().contains("rule description"));
    }

    @Test
    void rejectsMetricUnknownToDataSource() {
        Resource resource = resourceOf("""
                {"name": {"ko": "프리셋", "en": "preset"}, "rules": [{
                  "name": {"ko": "규칙", "en": "rule"}, "severity": "WARNING", "dataSource": "AGENT_STAT",
                  "checkIntervalSec": 300, "actionIntervalSec": 1800,
                  "conditions": {"type": "LEAF", "metric": "lcp_p75", "op": ">=", "threshold": 1, "windowSec": 300}
                }]}
                """);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> load(resource));
        assertTrue(e.getMessage().contains("lcp_p75"));
    }

    @Test
    void rejectsIntervalOutsidePickerOptions() {
        Resource resource = resourceOf("""
                {"name": {"ko": "프리셋", "en": "preset"}, "rules": [{
                  "name": {"ko": "규칙", "en": "rule"}, "severity": "WARNING", "dataSource": "AGENT_STAT",
                  "checkIntervalSec": 120, "actionIntervalSec": 1800,
                  "conditions": {"type": "LEAF", "metric": "sample_count", "op": ">=", "threshold": 1,
                                 "windowSec": 300, "aggregation": "COUNT"}
                }]}
                """);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> load(resource));
        assertTrue(e.getMessage().contains("checkIntervalSec"));
    }

    @Test
    void rejectsDuplicatePresetNamesAcrossFiles() {
        String preset = """
                {"name": {"ko": "프리셋", "en": "preset"}, "rules": [{
                  "name": {"ko": "규칙", "en": "rule"}, "severity": "WARNING", "dataSource": "AGENT_STAT",
                  "checkIntervalSec": 300, "actionIntervalSec": 1800,
                  "conditions": {"type": "LEAF", "metric": "sample_count", "op": ">=", "threshold": 1,
                                 "windowSec": 300, "aggregation": "COUNT"}
                }]}
                """;

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> load(resourceOf(preset), resourceOf(preset)));
        assertTrue(e.getMessage().contains("Duplicate preset name"));
    }

    @Test
    void acceptsEmptyCatalog() {
        // Presets are content a distribution supplies, not something this module ships,
        // so a deployment that offers none still starts -- it simply offers no preset.
        assertTrue(load().getPresets().isEmpty());
    }
}
