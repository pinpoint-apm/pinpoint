package com.navercorp.pinpoint.otlp.web.vo;

import java.util.Objects;

public record OtlpMetricNamesQueryParam(
    String serviceName,
    String applicationName,
    String agentId,
    String metricGroupName
) {
    public OtlpMetricNamesQueryParam {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(metricGroupName, "metricGroupName");
    }
}
