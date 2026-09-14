package com.navercorp.pinpoint.alarm.evaluation;

import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Evaluates a recursive condition tree against metric results.
 * <p>
 * The results map contains condition-specific metric query keys and their aggregated values.
 */
@Component
public class ConditionEvaluator {

    private static final Logger logger = LogManager.getLogger(ConditionEvaluator.class);

    public boolean evaluate(AlarmCondition node, Map<MetricQueryKey, Double> metricResults) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(metricResults, "metricResults");
        return evaluateNode(node, metricResults);
    }

    private boolean evaluateNode(AlarmCondition node, Map<MetricQueryKey, Double> metricResults) {
        if (node.isLeaf()) {
            return evaluateLeaf(node, metricResults);
        }

        if (node.isGroup()) {
            return evaluateGroup(node, metricResults);
        }

        throw new IllegalArgumentException("Unknown condition type: " + node.getType());
    }

    private boolean evaluateGroup(AlarmCondition node, Map<MetricQueryKey, Double> metricResults) {
        if (CollectionUtils.isEmpty(node.getCriteria())) {
            return false;
        }

        if (node.getOperator() == null) {
            throw new IllegalArgumentException("Group has no operator");
        }
        return switch (node.getOperator()) {
            case AND -> node.getCriteria().stream()
                    .allMatch(c -> evaluateNode(c, metricResults));
            case OR -> node.getCriteria().stream()
                    .anyMatch(c -> evaluateNode(c, metricResults));
        };
    }

    private boolean evaluateLeaf(AlarmCondition node, Map<MetricQueryKey, Double> metricResults) {
        MetricQueryKey key = MetricQueryKey.from(node);
        Double actualValue = metricResults.get(key);

        if (actualValue == null) {
            logger.warn("Metric '{}' returned null for evaluation", key.label());
            return false;
        }

        if (node.getTrigger() == AlarmCondition.Trigger.NEW_GROUP) {
            return actualValue > 0;
        }

        return compare(actualValue, node.getOp(), node.getThreshold());
    }

    private boolean compare(double actual, AlarmCondition.ComparisonOp op, double threshold) {
        return switch (op) {
            case GT -> actual > threshold;
            case GTE -> actual >= threshold;
            case LT -> actual < threshold;
            case LTE -> actual <= threshold;
            case EQ -> Double.compare(actual, threshold) == 0;
        };
    }
}
