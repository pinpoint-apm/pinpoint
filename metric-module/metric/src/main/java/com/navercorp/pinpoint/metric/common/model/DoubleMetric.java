package com.navercorp.pinpoint.metric.common.model;

import java.util.List;

public class DoubleMetric extends SystemMetric {
    private final double fieldValue;

    public DoubleMetric(String metricName, String hostName, String fieldName, double fieldValue, List<Tag> tags, long timestamp) {
        super(metricName, fieldName, hostName, tags, timestamp);
        this.fieldValue = fieldValue;
    }

    public double getFieldValue() {
        return fieldValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DoubleMetric{");
        sb.append("metric=").append(metricName);
        sb.append(", host=").append(hostName);
        sb.append(", field=").append(fieldName);
        sb.append(", value=").append(fieldValue);
        sb.append(", tags=").append(tags);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}
