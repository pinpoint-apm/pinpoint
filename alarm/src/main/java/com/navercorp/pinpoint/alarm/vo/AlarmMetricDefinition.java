package com.navercorp.pinpoint.alarm.vo;

import java.util.Set;

public record AlarmMetricDefinition(String value, String label, String trigger,
                                    Set<String> allowedAggregations) {

    public AlarmMetricDefinition(String value, String label) {
        this(value, label, null, null);
    }

    public AlarmMetricDefinition(String value, String label, String trigger) {
        this(value, label, trigger, null);
    }
}
