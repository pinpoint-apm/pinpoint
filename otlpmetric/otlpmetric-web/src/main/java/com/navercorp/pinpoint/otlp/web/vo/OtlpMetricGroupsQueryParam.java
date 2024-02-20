package com.navercorp.pinpoint.otlp.web.vo;

import java.util.Objects;

public record OtlpMetricGroupsQueryParam(
        String serviceId,
        String applicationId,
        String agentId
) {
    public OtlpMetricGroupsQueryParam {
        Objects.requireNonNull(applicationId, "applicationId");
    }
}
