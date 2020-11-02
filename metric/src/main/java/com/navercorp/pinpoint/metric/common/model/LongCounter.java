package com.navercorp.pinpoint.metric.common.model;

import java.util.List;

public class LongCounter extends SystemMetric {
    private final long fieldValue;

    public LongCounter(String metricName, String hostName, String fieldName, long fieldValue, List<Tag> tags, long timestamp) {
        super(metricName, hostName, fieldName, tags, timestamp);
        this.fieldValue = fieldValue;
    }

    public long getFieldValue() {
        return fieldValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LongCounter{");
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
