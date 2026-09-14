package com.navercorp.pinpoint.alarm.evaluation;

import com.navercorp.pinpoint.alarm.vo.AlarmCondition;

import java.util.ArrayList;
import java.util.List;

public final class ConditionUtils {

    private ConditionUtils() {
    }

    /**
     * Extracts all leaf nodes from a condition tree.
     */
    public static List<AlarmCondition> extractLeaves(AlarmCondition node) {
        List<AlarmCondition> leaves = new ArrayList<>();
        collectLeaves(node, leaves);
        return leaves;
    }

    private static void collectLeaves(AlarmCondition node, List<AlarmCondition> leaves) {
        if (node.isLeaf()) {
            leaves.add(node);
            return;
        }

        if (node.isGroup() && node.getCriteria() != null) {
            for (AlarmCondition child : node.getCriteria()) {
                collectLeaves(child, leaves);
            }
        }
    }
}
