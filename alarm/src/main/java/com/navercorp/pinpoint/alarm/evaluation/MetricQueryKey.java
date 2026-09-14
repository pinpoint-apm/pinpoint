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
