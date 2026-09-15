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
package com.navercorp.pinpoint.alarm.evaluation;

import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionEvaluatorTest {

    private final ConditionEvaluator evaluator = new ConditionEvaluator();

    @Test
    void testLeafGreaterThan() {
        AlarmCondition leaf = createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        assertTrue(evaluator.evaluate(leaf, metricResults(Map.of("error_count", 127.0))));
        assertFalse(evaluator.evaluate(leaf, metricResults(Map.of("error_count", 50.0))));
    }

    @Test
    void testLeafLessThan() {
        AlarmCondition leaf = createLeaf("cpu_usage", AlarmCondition.ComparisonOp.LT, 80.0);
        assertTrue(evaluator.evaluate(leaf, metricResults(Map.of("cpu_usage", 50.0))));
        assertFalse(evaluator.evaluate(leaf, metricResults(Map.of("cpu_usage", 90.0))));
    }

    @Test
    void testLeafMissingMetric() {
        AlarmCondition leaf = createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        assertFalse(evaluator.evaluate(leaf, metricResults(Map.of("other_metric", 200.0))));
    }

    @Test
    void testSameMetricDifferentWindowUsesMatchingKey() {
        AlarmCondition oneMinuteLeaf = createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        oneMinuteLeaf.setWindowSec(60);
        AlarmCondition fiveMinuteLeaf = createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        fiveMinuteLeaf.setWindowSec(300);

        assertFalse(evaluator.evaluate(oneMinuteLeaf, Map.of(MetricQueryKey.from(fiveMinuteLeaf), 150.0)));
        assertTrue(evaluator.evaluate(oneMinuteLeaf, Map.of(MetricQueryKey.from(oneMinuteLeaf), 150.0)));
    }

    @Test
    void testNewGroupIgnoresThreshold() {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("new_group_count");
        leaf.setTrigger(AlarmCondition.Trigger.NEW_GROUP);

        assertTrue(evaluator.evaluate(leaf, Map.of(MetricQueryKey.from(leaf), 1.0)));
        assertFalse(evaluator.evaluate(leaf, Map.of(MetricQueryKey.from(leaf), 0.0)));
    }

    @Test
    void testAndGroup() {
        AlarmCondition group = new AlarmCondition();
        group.setType(AlarmCondition.Type.GROUP);
        group.setOperator(AlarmCondition.Operator.AND);
        group.setCriteria(List.of(
                createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0),
                createLeaf("cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
        ));

        assertTrue(evaluator.evaluate(group, metricResults(Map.of("error_count", 150.0, "cpu_usage", 90.0))));
        assertFalse(evaluator.evaluate(group, metricResults(Map.of("error_count", 150.0, "cpu_usage", 50.0))));
    }

    @Test
    void testOrGroup() {
        AlarmCondition group = new AlarmCondition();
        group.setType(AlarmCondition.Type.GROUP);
        group.setOperator(AlarmCondition.Operator.OR);
        group.setCriteria(List.of(
                createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0),
                createLeaf("cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
        ));

        assertTrue(evaluator.evaluate(group, metricResults(Map.of("error_count", 150.0, "cpu_usage", 50.0))));
        assertTrue(evaluator.evaluate(group, metricResults(Map.of("error_count", 50.0, "cpu_usage", 90.0))));
        assertFalse(evaluator.evaluate(group, metricResults(Map.of("error_count", 50.0, "cpu_usage", 50.0))));
    }

    @Test
    void testNestedCondition() {
        // (error_count >= 100 AND cpu_usage >= 80) OR p99_latency > 5000
        AlarmCondition andGroup = new AlarmCondition();
        andGroup.setType(AlarmCondition.Type.GROUP);
        andGroup.setOperator(AlarmCondition.Operator.AND);
        andGroup.setCriteria(List.of(
                createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0),
                createLeaf("cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
        ));

        AlarmCondition orGroup = new AlarmCondition();
        orGroup.setType(AlarmCondition.Type.GROUP);
        orGroup.setOperator(AlarmCondition.Operator.OR);
        orGroup.setCriteria(List.of(
                andGroup,
                createLeaf("p99_latency", AlarmCondition.ComparisonOp.GT, 5000.0)
        ));

        // AND satisfied
        assertTrue(evaluator.evaluate(orGroup, metricResults(Map.of(
                "error_count", 150.0, "cpu_usage", 90.0, "p99_latency", 1000.0
        ))));

        // p99 satisfied
        assertTrue(evaluator.evaluate(orGroup, metricResults(Map.of(
                "error_count", 50.0, "cpu_usage", 50.0, "p99_latency", 6000.0
        ))));

        // nothing satisfied
        assertFalse(evaluator.evaluate(orGroup, metricResults(Map.of(
                "error_count", 50.0, "cpu_usage", 50.0, "p99_latency", 1000.0
        ))));
    }

    private AlarmCondition createLeaf(String metric, AlarmCondition.ComparisonOp op, double threshold) {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric(metric);
        leaf.setOp(op);
        leaf.setThreshold(threshold);
        leaf.setWindowSec(300);
        leaf.setAggregation(AlarmCondition.Aggregation.COUNT);
        return leaf;
    }

    private Map<MetricQueryKey, Double> metricResults(Map<String, Double> values) {
        Map<MetricQueryKey, Double> results = new HashMap<>();
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            results.put(MetricQueryKey.from(createLeaf(entry.getKey(), AlarmCondition.ComparisonOp.GTE, 0.0)), entry.getValue());
        }
        return results;
    }
}
