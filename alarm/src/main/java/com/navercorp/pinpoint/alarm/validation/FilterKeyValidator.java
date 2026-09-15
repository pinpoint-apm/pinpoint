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

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.evaluation.ConditionUtils;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmMetricDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Validates AlarmFilter fields and metric-aggregation combinations against per-dataSource whitelists.
 * <p>
 * Filter keys are injected into MyBatis queries via ${f.key} (string substitution),
 * so only known column names are permitted.
 */
@Component
public class FilterKeyValidator {

    public List<AlarmFilter> validateFilters(AlarmDataSource dataSource, List<AlarmFilter> filters) {
        return validateFilterKeys(dataSource, filters);
    }

    public static List<AlarmFilter> validateFilterKeys(AlarmDataSource dataSource, List<AlarmFilter> filters) {
        Objects.requireNonNull(dataSource, "dataSource must not be null");
        if (CollectionUtils.isEmpty(filters)) {
            return filters;
        }
        if (filters.size() > AlarmValidationConstants.MAX_FILTER_COUNT) {
            throw new IllegalArgumentException(
                    "Filter count exceeds maximum of " + AlarmValidationConstants.MAX_FILTER_COUNT);
        }
        for (AlarmFilter filter : filters) {
            if (filter == null) {
                throw new IllegalArgumentException("Filter must not be null");
            }
            if (filter.getKey() == null || !dataSource.filterKeys().contains(filter.getKey())) {
                throw new IllegalArgumentException(
                        "Invalid filter key '" + filter.getKey() + "' for dataSource " + dataSource.name()
                        + ". Allowed: " + dataSource.filterKeys());
            }
            // invalid op values are rejected at deserialization; only presence needs checking here
            if (filter.getOp() == null) {
                throw new IllegalArgumentException("Filter must have an op");
            }
            if (StringUtils.isBlank(filter.getValue())) {
                throw new IllegalArgumentException("Filter value must not be blank");
            }
            if (filter.getValue().length() > AlarmValidationConstants.MAX_FILTER_VALUE_LENGTH) {
                throw new IllegalArgumentException(
                        "Filter value exceeds maximum length of " + AlarmValidationConstants.MAX_FILTER_VALUE_LENGTH);
            }
        }
        return filters;
    }

    public void validateConditions(AlarmDataSource dataSource, AlarmCondition condition) {
        Objects.requireNonNull(dataSource, "dataSource must not be null");
        if (condition == null) {
            return;
        }
        List<AlarmCondition> leaves = ConditionUtils.extractLeaves(condition);
        for (AlarmCondition leaf : leaves) {
            validateLeafMetric(dataSource, leaf);
        }
    }

    private void validateLeafMetric(AlarmDataSource dataSource, AlarmCondition leaf) {
        String metric = leaf.getMetric();
        AlarmMetricDefinition definition = findMetricDefinition(dataSource, metric);
        if (definition == null) {
            throw new IllegalArgumentException(
                    "Invalid metric '" + metric + "' for dataSource " + dataSource.name());
        }

        // The catalog is string-based; enum fields cross the boundary via name()
        String expectedTrigger = definition.trigger();
        String actualTrigger = leaf.getTrigger() != null ? leaf.getTrigger().name() : null;
        if (!Objects.equals(expectedTrigger, actualTrigger)) {
            if (expectedTrigger == null) {
                throw new IllegalArgumentException(
                        "Metric '" + metric + "' does not support trigger '" + actualTrigger + "'");
            }
            throw new IllegalArgumentException(
                    "Metric '" + metric + "' requires trigger '" + expectedTrigger + "'");
        }

        AlarmCondition.Aggregation aggregation = leaf.getAggregation();
        if (expectedTrigger != null) {
            if (aggregation == null) {
                return;
            }
            throw new IllegalArgumentException(
                    "Metric '" + metric + "' with trigger '" + expectedTrigger + "' must not define aggregation");
        }
        Set<String> allowed = definition.allowedAggregations();
        if (CollectionUtils.isEmpty(allowed)) {
            if (aggregation == null) {
                return;
            }
            throw new IllegalArgumentException("Metric '" + metric + "' does not support aggregation");
        }
        if (aggregation == null) {
            if (allowed.size() == 1) {
                leaf.setAggregation(AlarmCondition.Aggregation.valueOf(allowed.iterator().next()));
                return;
            }
            throw new IllegalArgumentException(
                    "Metric '" + metric + "' requires aggregation. Allowed: " + allowed);
        }
        if (!allowed.contains(aggregation.name())) {
            throw new IllegalArgumentException(
                    "Invalid aggregation '" + aggregation + "' for metric '" + metric
                    + "'. Allowed: " + allowed);
        }
    }

    private AlarmMetricDefinition findMetricDefinition(AlarmDataSource dataSource,
                                                                   String metric) {
        if (metric == null) {
            return null;
        }
        for (AlarmMetricDefinition definition : dataSource.metrics()) {
            if (metric.equals(definition.value())) {
                return definition;
            }
        }
        return null;
    }
}
