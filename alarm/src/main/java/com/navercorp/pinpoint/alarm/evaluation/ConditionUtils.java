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
