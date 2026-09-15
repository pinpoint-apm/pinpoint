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
package com.navercorp.pinpoint.alarm.vo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmJsonMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void alarmCondition_deserializesLeafEnums() throws Exception {
        String json = """
                {
                  "type": "LEAF",
                  "metric": "error_count",
                  "trigger": "NEW_GROUP",
                  "op": ">=",
                  "threshold": 100,
                  "windowSec": 300,
                  "aggregation": "COUNT",
                  "thresholdType": "BASELINE_CHANGE",
                  "baselinePeriodSec": 3600
                }
                """;

        AlarmCondition condition = objectMapper.readValue(json, AlarmCondition.class);

        assertEquals(AlarmCondition.Type.LEAF, condition.getType());
        assertEquals("error_count", condition.getMetric());
        assertEquals(AlarmCondition.Trigger.NEW_GROUP, condition.getTrigger());
        assertEquals(AlarmCondition.ComparisonOp.GTE, condition.getOp());
        assertEquals(100.0, condition.getThreshold());
        assertEquals(300, condition.getWindowSec());
        assertEquals(AlarmCondition.Aggregation.COUNT, condition.getAggregation());
        assertEquals(AlarmCondition.ThresholdType.BASELINE_CHANGE, condition.getThresholdType());
        assertEquals(3600, condition.getBaselinePeriodSec());
    }

    @Test
    void alarmCondition_deserializesGroupTree() throws Exception {
        String json = """
                {
                  "type": "GROUP",
                  "operator": "AND",
                  "criteria": [
                    {"type": "LEAF", "metric": "error_count", "op": ">", "threshold": 100, "windowSec": 300},
                    {"type": "LEAF", "metric": "cpu_usage", "op": "<=", "threshold": 80, "windowSec": 300}
                  ]
                }
                """;

        AlarmCondition condition = objectMapper.readValue(json, AlarmCondition.class);

        assertEquals(AlarmCondition.Operator.AND, condition.getOperator());
        assertEquals(2, condition.getCriteria().size());
        assertEquals(AlarmCondition.ComparisonOp.GT, condition.getCriteria().get(0).getOp());
        assertEquals(AlarmCondition.ComparisonOp.LTE, condition.getCriteria().get(1).getOp());
    }

    @Test
    void alarmCondition_acceptsDoubleEqualsAliasAndNormalizesOnWrite() throws Exception {
        String json = """
                {"type": "LEAF", "metric": "error_count", "op": "==", "threshold": 100, "windowSec": 300}
                """;

        AlarmCondition condition = objectMapper.readValue(json, AlarmCondition.class);
        assertEquals(AlarmCondition.ComparisonOp.EQ, condition.getOp());

        String written = objectMapper.writeValueAsString(condition);
        assertTrue(written.contains("\"op\":\"=\""), written);
    }

    @Test
    void alarmCondition_roundTrip() throws Exception {
        String json = """
                {
                  "type": "GROUP",
                  "operator": "OR",
                  "criteria": [
                    {"type": "LEAF", "metric": "error_count", "op": ">=", "threshold": 100, "windowSec": 300, "aggregation": "SUM"}
                  ]
                }
                """;

        AlarmCondition condition = objectMapper.readValue(json, AlarmCondition.class);
        AlarmCondition reread = objectMapper.readValue(objectMapper.writeValueAsString(condition), AlarmCondition.class);

        assertEquals(condition.getOperator(), reread.getOperator());
        AlarmCondition leaf = reread.getCriteria().get(0);
        assertEquals(AlarmCondition.ComparisonOp.GTE, leaf.getOp());
        assertEquals(AlarmCondition.Aggregation.SUM, leaf.getAggregation());
    }

    @Test
    void alarmCondition_rejectsInvalidType() {
        String json = """
                {"type": "branch", "metric": "error_count", "op": ">", "threshold": 100, "windowSec": 300}
                """;

        assertThrows(JsonMappingException.class,
                () -> objectMapper.readValue(json, AlarmCondition.class));
    }

    @Test
    void alarmCondition_rejectsInvalidOp() {
        String json = """
                {"type": "LEAF", "metric": "error_count", "op": "~=", "threshold": 100, "windowSec": 300}
                """;

        assertThrows(JsonMappingException.class,
                () -> objectMapper.readValue(json, AlarmCondition.class));
    }

    @Test
    void alarmCondition_acceptsLowercaseTypeAndNormalizesOnWrite() throws Exception {
        String json = """
                {"type": "leaf", "metric": "error_count", "op": ">", "threshold": 100, "windowSec": 300}
                """;

        AlarmCondition condition = objectMapper.readValue(json, AlarmCondition.class);

        assertEquals(AlarmCondition.Type.LEAF, condition.getType());
        assertEquals("LEAF", objectMapper.valueToTree(condition).path("type").asText());
    }

    @Test
    void alarmCondition_rejectsInvalidOperator() {
        String json = """
                {"type": "GROUP", "operator": "XOR", "criteria": []}
                """;

        assertThrows(JsonMappingException.class,
                () -> objectMapper.readValue(json, AlarmCondition.class));
    }

    @Test
    void alarmCondition_rejectsInvalidAggregation() {
        String json = """
                {"type": "LEAF", "metric": "error_count", "op": ">", "threshold": 100, "windowSec": 300, "aggregation": "MEDIAN"}
                """;

        assertThrows(JsonMappingException.class,
                () -> objectMapper.readValue(json, AlarmCondition.class));
    }

    @Test
    void alarmCondition_rejectsUnknownFieldEvenWhenMapperIgnoresUnknownProperties() {
        String json = """
                {
                  "type": "LEAF",
                  "metric": "error_count",
                  "op": ">=",
                  "threshold": 100,
                  "windowSec": 300,
                  "unknown": "value"
                }
                """;

        assertThrows(JsonMappingException.class,
                () -> objectMapper.readValue(json, AlarmCondition.class));
    }

    @Test
    void alarmFilter_writesOpAsName() throws Exception {
        AlarmFilter filter = new AlarmFilter("environment", AlarmFilter.Op.NOT_CONTAINS, "staging");

        String written = objectMapper.writeValueAsString(filter);
        assertTrue(written.contains("\"op\":\"NOT_CONTAINS\""), written);
    }

    @Test
    void alarmFilter_rejectsInvalidOp() {
        String json = """
                {"key": "environment", "op": "REGEX", "value": "prod.*"}
                """;

        assertThrows(JsonMappingException.class,
                () -> objectMapper.readValue(json, AlarmFilter.class));
    }

    @Test
    void alarmFilter_rejectsUnknownFieldEvenWhenMapperIgnoresUnknownProperties() {
        String json = """
                {
                  "key": "environment",
                  "op": "EQ",
                  "value": "production",
                  "unknown": "value"
                }
                """;

        assertThrows(JsonMappingException.class,
                () -> objectMapper.readValue(json, AlarmFilter.class));
    }
}
