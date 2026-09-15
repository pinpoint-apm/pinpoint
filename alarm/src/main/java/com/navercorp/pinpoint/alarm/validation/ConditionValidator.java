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

import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

/**
 * Validates condition tree structure before saving.
 * <p>
 * Max depth is 2:
 * - depth 0: leaf
 * - depth 1: group → leaves
 * - depth 2: group → group → leaves
 */
@Component
public class ConditionValidator {

    private static final int MAX_DEPTH = AlarmValidationConstants.MAX_CONDITION_DEPTH;
    private static final int MAX_NODE_COUNT = AlarmValidationConstants.MAX_CONDITION_NODE_COUNT;
    private static final int MAX_LEAVES_PER_GROUP = AlarmValidationConstants.MAX_LEAVES_PER_GROUP;
    private static final int MAX_GROUPS_PER_GROUP = AlarmValidationConstants.MAX_GROUPS_PER_GROUP;
    private static final int MIN_WINDOW_SEC = AlarmValidationConstants.MIN_WINDOW_SEC;

    /**
     * Enforces node-count and depth limits across the recursive traversal.
     * Immutable per depth level — {@link #next()} returns a limiter for the next
     * depth while the node counter is shared across the whole tree. The root is at depth 0.
     */
    private static final class TreeLimiter {
        private final int maxNodeCount;
        private final int maxDepth;
        private final MutableInt nodeCount;
        private final int depth;

        TreeLimiter(int maxNodeCount, int maxDepth) {
            this(maxNodeCount, maxDepth, new MutableInt(), 0);
        }

        private TreeLimiter(int maxNodeCount, int maxDepth, MutableInt nodeCount, int depth) {
            this.maxNodeCount = maxNodeCount;
            this.maxDepth = maxDepth;
            this.nodeCount = nodeCount;
            this.depth = depth;
        }

        /**
         * Records entry of one node and enforces the tree limits.
         */
        void enterNode() {
            if (nodeCount.incrementAndGet() > maxNodeCount) {
                throw new IllegalArgumentException("Condition tree exceeds maximum node count of " + maxNodeCount);
            }
            if (depth > maxDepth) {
                throw new IllegalArgumentException("Condition tree exceeds max depth of " + maxDepth);
            }
        }

        TreeLimiter next() {
            return new TreeLimiter(maxNodeCount, maxDepth, nodeCount, depth + 1);
        }
    }

    public void validate(AlarmCondition condition) {
        Objects.requireNonNull(condition, "condition must not be null");
        validateNode(condition, new TreeLimiter(MAX_NODE_COUNT, MAX_DEPTH));
    }

    private void validateNode(AlarmCondition node, TreeLimiter limiter) {
        limiter.enterNode();

        // invalid type values are rejected at deserialization; only presence needs checking here
        if (node.getType() == null) {
            throw new IllegalArgumentException("Condition node must have a type");
        }

        if (node.isGroup()) {
            validateGroup(node, limiter);
        } else if (node.isLeaf()) {
            validateLeaf(node);
        }
    }

    private void validateGroup(AlarmCondition node, TreeLimiter limiter) {
        // enum fields are validated at deserialization; only presence needs checking here
        if (node.getOperator() == null) {
            throw new IllegalArgumentException("Group must have an operator");
        }

        if (CollectionUtils.isEmpty(node.getCriteria())) {
            throw new IllegalArgumentException("Group must have at least one criterion");
        }

        if (node.getCriteria().stream().anyMatch(Objects::isNull)) {
            // @Valid does not reject a null element, and the counts below dereference one.
            throw new IllegalArgumentException("Group criteria must not contain a null criterion");
        }

        long leafCount = node.getCriteria().stream().filter(AlarmCondition::isLeaf).count();
        long groupCount = node.getCriteria().stream().filter(AlarmCondition::isGroup).count();
        if (leafCount > MAX_LEAVES_PER_GROUP) {
            throw new IllegalArgumentException(
                    "Group exceeds max leaf count of " + MAX_LEAVES_PER_GROUP);
        }
        if (groupCount > MAX_GROUPS_PER_GROUP) {
            throw new IllegalArgumentException(
                    "Group exceeds max sub-group count of " + MAX_GROUPS_PER_GROUP);
        }

        for (AlarmCondition child : node.getCriteria()) {
            validateNode(child, limiter.next());
        }
    }

    private void validateLeaf(AlarmCondition node) {
        if (StringUtils.isBlank(node.getMetric())) {
            throw new IllegalArgumentException("Leaf must have a metric");
        }

        if (node.getThresholdType() == AlarmCondition.ThresholdType.BASELINE_CHANGE) {
            // The evaluator compares the metric value against the threshold directly, so a
            // baseline rule would silently behave as an absolute one. Reject it until the
            // baseline query has somewhere to come from.
            throw new IllegalArgumentException("thresholdType BASELINE_CHANGE is not supported");
        }

        if (!isNewGroupTrigger(node)) {
            if (node.getOp() == null) {
                throw new IllegalArgumentException("Leaf must have an op");
            }
            if (node.getThreshold() == null) {
                throw new IllegalArgumentException("Leaf must have a threshold");
            }
            if (node.getWindowSec() == null || node.getWindowSec() < MIN_WINDOW_SEC) {
                throw new IllegalArgumentException("windowSec must be at least " + MIN_WINDOW_SEC);
            }
        }
    }

    private static boolean isNewGroupTrigger(AlarmCondition node) {
        return node.getTrigger() == AlarmCondition.Trigger.NEW_GROUP;
    }
}
