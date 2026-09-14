package com.navercorp.pinpoint.alarm.validation;

import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConditionValidatorTest {

    private final ConditionValidator validator = new ConditionValidator();

    @Test
    void validLeaf() {
        assertDoesNotThrow(() -> validator.validate(createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0)));
    }

    @Test
    void validGroupDepth1() {
        AlarmCondition group = new AlarmCondition();
        group.setType(AlarmCondition.Type.GROUP);
        group.setOperator(AlarmCondition.Operator.AND);
        group.setCriteria(List.of(
                createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0),
                createLeaf("cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
        ));
        assertDoesNotThrow(() -> validator.validate(group));
    }

    @Test
    void validGroupDepth2() {
        AlarmCondition innerGroup = new AlarmCondition();
        innerGroup.setType(AlarmCondition.Type.GROUP);
        innerGroup.setOperator(AlarmCondition.Operator.AND);
        innerGroup.setCriteria(List.of(
                createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0),
                createLeaf("cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
        ));

        AlarmCondition outerGroup = new AlarmCondition();
        outerGroup.setType(AlarmCondition.Type.GROUP);
        outerGroup.setOperator(AlarmCondition.Operator.OR);
        outerGroup.setCriteria(List.of(
                innerGroup,
                createLeaf("p99_latency", AlarmCondition.ComparisonOp.GT, 5000.0)
        ));

        assertDoesNotThrow(() -> validator.validate(outerGroup));
    }

    @Test
    void exceedsMaxDepth() {
        AlarmCondition level2 = new AlarmCondition();
        level2.setType(AlarmCondition.Type.GROUP);
        level2.setOperator(AlarmCondition.Operator.AND);
        level2.setCriteria(List.of(createLeaf("a", AlarmCondition.ComparisonOp.GTE, 1.0)));

        AlarmCondition level1 = new AlarmCondition();
        level1.setType(AlarmCondition.Type.GROUP);
        level1.setOperator(AlarmCondition.Operator.AND);
        level1.setCriteria(List.of(level2));

        AlarmCondition level0 = new AlarmCondition();
        level0.setType(AlarmCondition.Type.GROUP);
        level0.setOperator(AlarmCondition.Operator.AND);
        level0.setCriteria(List.of(level1));

        assertThrows(IllegalArgumentException.class, () -> validator.validate(level0));
    }

    // The evaluator compares the metric value against the threshold directly, so
    // accepting a baseline rule would let it run as an absolute one without saying so.
    @Test
    void rejectsBaselineChangeThreshold() {
        AlarmCondition leaf = createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0);
        leaf.setThresholdType(AlarmCondition.ThresholdType.BASELINE_CHANGE);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(leaf));
        assertTrue(e.getMessage().contains("BASELINE_CHANGE"), e.getMessage());
    }

    // @Valid lets a null element through, and the leaf/group counts dereference it.
    @Test
    void rejectsNullCriterion() {
        AlarmCondition group = new AlarmCondition();
        group.setType(AlarmCondition.Type.GROUP);
        group.setOperator(AlarmCondition.Operator.AND);
        group.setCriteria(Arrays.asList(createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 1.0), null));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(group));
        assertTrue(e.getMessage().contains("null criterion"), e.getMessage());
    }

    @Test
    void leafMissingMetric() {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setOp(AlarmCondition.ComparisonOp.GTE);
        leaf.setThreshold(100.0);
        leaf.setWindowSec(300);

        assertThrows(IllegalArgumentException.class, () -> validator.validate(leaf));
    }

    @Test
    void leafMissingThreshold() {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("error_count");
        leaf.setOp(AlarmCondition.ComparisonOp.GTE);
        leaf.setWindowSec(300);

        assertThrows(IllegalArgumentException.class, () -> validator.validate(leaf));
    }

    @Test
    void newGroupLeaf_noWindowSec_valid() {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setTrigger(AlarmCondition.Trigger.NEW_GROUP);
        leaf.setMetric("new_group_count");
        leaf.setOp(AlarmCondition.ComparisonOp.GT);
        leaf.setThreshold(0.0);

        assertDoesNotThrow(() -> validator.validate(leaf));
    }

    @Test
    void newGroupLeaf_noOpThresholdWindowSec_valid() {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setTrigger(AlarmCondition.Trigger.NEW_GROUP);
        leaf.setMetric("new_group_count");

        assertDoesNotThrow(() -> validator.validate(leaf));
    }

    @Test
    void regularLeaf_missingWindowSec_invalid() {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("error_count");
        leaf.setOp(AlarmCondition.ComparisonOp.GTE);
        leaf.setThreshold(100.0);
        // windowSec missing

        assertThrows(IllegalArgumentException.class, () -> validator.validate(leaf));
    }

    @Test
    void leafMissingOp() {
        // invalid op strings are rejected at deserialization; the validator checks presence
        AlarmCondition leaf = createLeaf("error_count", null, 100.0);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(leaf));
    }

    @Test
    void emptyGroupCriteria() {
        AlarmCondition group = new AlarmCondition();
        group.setType(AlarmCondition.Type.GROUP);
        group.setOperator(AlarmCondition.Operator.AND);
        group.setCriteria(List.of());

        assertThrows(IllegalArgumentException.class, () -> validator.validate(group));
    }

    @Test
    void maxNodeCount_valid() {
        AlarmCondition group = createGroup(
                createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0),
                createLeaf("affected_user_count", AlarmCondition.ComparisonOp.GTE, 10.0),
                createLeaf("p99_latency", AlarmCondition.ComparisonOp.GT, 5000.0),
                createGroup(
                        createLeaf("cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0),
                        createLeaf("jvm_cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
                ),
                createGroup(
                        createLeaf("heap_usage", AlarmCondition.ComparisonOp.GTE, 80.0),
                        createLeaf("non_heap_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
                )
        );

        assertDoesNotThrow(() -> validator.validate(group));
    }

    @Test
    void exceedsMaxNodeCount_invalid() {
        AlarmCondition group = createGroup(
                createLeaf("error_count", AlarmCondition.ComparisonOp.GTE, 100.0),
                createLeaf("affected_user_count", AlarmCondition.ComparisonOp.GTE, 10.0),
                createLeaf("p99_latency", AlarmCondition.ComparisonOp.GT, 5000.0),
                createGroup(
                        createLeaf("cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0),
                        createLeaf("jvm_cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0),
                        createLeaf("system_cpu_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
                ),
                createGroup(
                        createLeaf("heap_usage", AlarmCondition.ComparisonOp.GTE, 80.0),
                        createLeaf("non_heap_usage", AlarmCondition.ComparisonOp.GTE, 80.0)
                )
        );

        assertThrows(IllegalArgumentException.class, () -> validator.validate(group));
    }

    private AlarmCondition createGroup(AlarmCondition... criteria) {
        AlarmCondition group = new AlarmCondition();
        group.setType(AlarmCondition.Type.GROUP);
        group.setOperator(AlarmCondition.Operator.AND);
        group.setCriteria(List.of(criteria));
        return group;
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
}
