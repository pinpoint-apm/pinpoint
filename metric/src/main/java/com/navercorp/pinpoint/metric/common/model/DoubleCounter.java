package com.navercorp.pinpoint.metric.common.model;

import java.util.List;

public class DoubleCounter extends SystemMetric {
    private final double fieldValue;

    public DoubleCounter(String metricName, String hostName, String fieldName, double fieldValue, List<Tag> tags, long timestamp) {
        super(metricName, hostName, fieldName, tags, timestamp);
        this.fieldValue = fieldValue;
    }

    public double getFieldValue() {
        return fieldValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DoubleCounter{");
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
