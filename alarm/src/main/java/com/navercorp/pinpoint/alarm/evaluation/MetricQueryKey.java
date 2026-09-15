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

/**
 * Identifies a metric query for a single alarm condition.
 */
public record MetricQueryKey(String metric, AlarmCondition.Trigger trigger, Integer windowSec, AlarmCondition.Aggregation aggregation) {

    public static MetricQueryKey from(AlarmCondition condition) {
        return new MetricQueryKey(
                condition.getMetric(),
                condition.getTrigger(),
                condition.getWindowSec(),
                condition.getAggregation()
        );
    }

    public String label() {
        StringBuilder label = new StringBuilder(metric != null ? metric : "");
        String separator = "[";
        if (trigger != null) {
            label.append(separator).append("trigger=").append(trigger);
            separator = ", ";
        }
        if (windowSec != null) {
            label.append(separator).append("window=").append(windowSec).append("s");
            separator = ", ";
        }
        if (aggregation != null) {
            label.append(separator).append("aggregation=").append(aggregation);
            separator = ", ";
        }
        if (separator.length() > 1) {
            label.append("]");
        }
        return label.toString();
    }
}
