package com.navercorp.pinpoint.otlp.web.vo;

import java.util.Objects;

public record OtlpMetricGroupsQueryParam(
        String serviceName,
        String applicationName,
        String agentId
) {
    public OtlpMetricGroupsQueryParam {
        Objects.requireNonNull(applicationName, "applicationName");
    }
}
