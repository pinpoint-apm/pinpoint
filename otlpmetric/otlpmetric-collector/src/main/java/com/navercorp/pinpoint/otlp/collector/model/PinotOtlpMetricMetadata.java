package com.navercorp.pinpoint.otlp.collector.model;

import java.util.Objects;

public record PinotOtlpMetricMetadata(
    String serviceName,
    String applicationName,
    String agentId,
    String metricGroupName,
    String metricName,
    String fieldName,
    String unit,
    String description,
    int metricType,
    int dataType,
    int aggreFunc,
    int aggreTemporality,
    String rawTags,
    Long startTime,
    Long saveTime,
    String version
) {
    public PinotOtlpMetricMetadata {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(metricGroupName, "metricGroupName");
        Objects.requireNonNull(metricName, "metricName");
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(unit, "unit");
        Objects.requireNonNull(saveTime, "saveTime");
    }
}
