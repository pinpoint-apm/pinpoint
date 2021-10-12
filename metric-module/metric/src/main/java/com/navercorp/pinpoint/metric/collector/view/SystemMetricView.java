package com.navercorp.pinpoint.metric.collector.view;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;

import java.util.Objects;

public class SystemMetricView {
    private final String hostGroupName;
    private final SystemMetric metric;

    public SystemMetricView(String hostGroupName, SystemMetric metric) {
        this.hostGroupName = Objects.requireNonNull(hostGroupName, "hostGroupName");
        this.metric = Objects.requireNonNull(metric, "metric");
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    @JsonUnwrapped
    public SystemMetric getMetric() {
        return metric;
    }
}
