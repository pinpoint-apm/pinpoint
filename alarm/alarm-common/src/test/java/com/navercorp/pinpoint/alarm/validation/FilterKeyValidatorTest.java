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
package com.navercorp.pinpoint.alarm.validation;

import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterKeyValidatorTest {

    private final FilterKeyValidator validator = new FilterKeyValidator();

    @Test
    void validateFilters_allowsDataSourceFilterKey() {
        List<AlarmFilter> filters = List.of(new AlarmFilter("agent", AlarmFilter.Op.EQ, "production"));

        List<AlarmFilter> result = validator.validateFilters(TestAlarmDataSource.AGENT_STAT, filters);

        assertSame(filters, result);
    }

    @Test
    void validateFilters_rejectsFilterKeyFromOtherDataSource() {
        List<AlarmFilter> filters = List.of(new AlarmFilter("callee", AlarmFilter.Op.EQ, "web"));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateFilters(TestAlarmDataSource.AGENT_STAT, filters));
    }

    @Test
    void validateFilters_rejectsTooManyFilters() {
        List<AlarmFilter> filters = List.of(
                new AlarmFilter("agent", AlarmFilter.Op.EQ, "production"),
                new AlarmFilter("service_type", AlarmFilter.Op.EQ, "1.0.0"),
                new AlarmFilter("exceptionType", AlarmFilter.Op.EQ, "Error"),
                new AlarmFilter("exceptionMessage", AlarmFilter.Op.CONTAINS, "failed"),
                new AlarmFilter("userIp", AlarmFilter.Op.EQ, "127.0.0.1"),
                new AlarmFilter("agent", AlarmFilter.Op.NEQ, "staging")
        );

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateFilters(TestAlarmDataSource.AGENT_STAT, filters));
    }

    @Test
    void validateFilters_rejectsNullFilterOp() {
        List<AlarmFilter> filters = List.of(new AlarmFilter("agent", null, "prod.*"));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateFilters(TestAlarmDataSource.AGENT_STAT, filters));
    }

    @Test
    void validateFilters_rejectsTooLongFilterValue() {
        String value = "x".repeat(AlarmValidationConstants.MAX_FILTER_VALUE_LENGTH + 1);
        List<AlarmFilter> filters = List.of(new AlarmFilter("agent", AlarmFilter.Op.EQ, value));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateFilters(TestAlarmDataSource.AGENT_STAT, filters));
    }

    @Test
    void validateConditions_allowsMetricForDataSource() {
        AlarmCondition leaf = createLeaf("sample_count", null, AlarmCondition.Aggregation.COUNT);

        assertDoesNotThrow(() ->
                validator.validateConditions(TestAlarmDataSource.AGENT_STAT, leaf));
    }

    @Test
    void validateConditions_defaultsSingleAllowedAggregation() {
        AlarmCondition leaf = createLeaf("sample_count", null, null);

        assertDoesNotThrow(() ->
                validator.validateConditions(TestAlarmDataSource.AGENT_STAT, leaf));
        assertEquals(AlarmCondition.Aggregation.COUNT, leaf.getAggregation());
    }

    @Test
    void validateConditions_rejectsMetricFromOtherDataSource() {
        AlarmCondition leaf = createLeaf("total_count", null, AlarmCondition.Aggregation.SUM);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateConditions(TestAlarmDataSource.AGENT_STAT, leaf));
    }

    @Test
    void validateConditions_rejectsInvalidAggregationForMetric() {
        AlarmCondition leaf = createLeaf("sample_count", null, AlarmCondition.Aggregation.SUM);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateConditions(TestAlarmDataSource.AGENT_STAT, leaf));
    }

    @Test
    void validateConditions_requiresCatalogTrigger() {
        AlarmCondition leaf = createLeaf("deadlock_count", null, null);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateConditions(TestAlarmDataSource.AGENT_STAT, leaf));
    }

    @Test
    void validateConditions_rejectsUnexpectedTrigger() {
        AlarmCondition leaf = createLeaf("sample_count", AlarmCondition.Trigger.NEW_GROUP, null);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateConditions(TestAlarmDataSource.AGENT_STAT, leaf));
    }

    @Test
    void validateConditions_rejectsAggregationForTriggerMetric() {
        AlarmCondition leaf = createLeaf("deadlock_count", AlarmCondition.Trigger.NEW_GROUP, AlarmCondition.Aggregation.COUNT);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateConditions(TestAlarmDataSource.AGENT_STAT, leaf));
    }

    @Test
    void validateConditions_rejectsAggregationForUnsupportedMetric() {
        AlarmCondition leaf = createLeaf("apdex_score", null, AlarmCondition.Aggregation.SUM);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateConditions(TestAlarmDataSource.APPLICATION_RESPONSE, leaf));
    }

    @Test
    void validateConditions_allowsTriggerMetric() {
        AlarmCondition leaf = createLeaf("deadlock_count", AlarmCondition.Trigger.NEW_GROUP, null);

        assertDoesNotThrow(() ->
                validator.validateConditions(TestAlarmDataSource.AGENT_STAT, leaf));
    }

    private AlarmCondition createLeaf(String metric, AlarmCondition.Trigger trigger, AlarmCondition.Aggregation aggregation) {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric(metric);
        leaf.setTrigger(trigger);
        leaf.setAggregation(aggregation);
        return leaf;
    }
}
