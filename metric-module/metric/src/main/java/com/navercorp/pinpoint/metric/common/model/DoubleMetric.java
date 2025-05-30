package com.navercorp.pinpoint.metric.common.model;

import java.util.List;

public class DoubleMetric extends SystemMetric {
    private final double fieldValue;

    public DoubleMetric(String metricName, String hostName, String fieldName, double fieldValue, List<Tag> tags, long eventTime) {
        super(metricName, fieldName, hostName, tags, eventTime);
        this.fieldValue = fieldValue;
    }

    public double getFieldValue() {
        return fieldValue;
    }

    @Override
    public String toString() {
        return "DoubleMetric{" +
                "metric=" + metricName +
                ", host=" + hostName +
                ", field=" + fieldName +
                ", value=" + fieldValue +
                ", tags=" + tags +
                ", eventTime=" + eventTime +
                '}';
    }
}
