package com.navercorp.pinpoint.otlp.web.vo;

import java.util.Objects;

public record OtlpMetricNamesQueryParam(
    String serviceId,
    String applicationId,
    String agentId,
    String metricGroupName
) {
    public OtlpMetricNamesQueryParam {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(metricGroupName, "metricGroupName");
    }
}
