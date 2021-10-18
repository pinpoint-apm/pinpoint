package com.navercorp.pinpoint.metric.common.model;

import java.util.List;

public class LongMetric extends SystemMetric {
    private final long fieldValue;

    public LongMetric(String metricName, String hostName, String fieldName, long fieldValue, List<Tag> tags, long eventTime) {
        super(metricName, fieldName, hostName, tags, eventTime);
        this.fieldValue = fieldValue;
    }

    public long getFieldValue() {
        return fieldValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LongMetric{");
        sb.append("metric=").append(metricName);
        sb.append(", host=").append(hostName);
        sb.append(", field=").append(fieldName);
        sb.append(", value=").append(fieldValue);
        sb.append(", tags=").append(tags);
        sb.append(", eventTime=").append(eventTime);
        sb.append('}');
        return sb.toString();
    }
}
