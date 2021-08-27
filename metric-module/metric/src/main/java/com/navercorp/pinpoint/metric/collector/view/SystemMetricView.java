package com.navercorp.pinpoint.metric.collector.view;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;

import java.util.Objects;

public class SystemMetricView {
    private final String hostGroupId;
    private final SystemMetric metric;

    public SystemMetricView(String hostGroupId, SystemMetric metric) {
        this.hostGroupId = Objects.requireNonNull(hostGroupId, "hostGroupId");
        this.metric = Objects.requireNonNull(metric, "metric");
    }

    public String getHostGroupId() {
        return hostGroupId;
    }

    @JsonUnwrapped
    public SystemMetric getMetric() {
        return metric;
    }
}
