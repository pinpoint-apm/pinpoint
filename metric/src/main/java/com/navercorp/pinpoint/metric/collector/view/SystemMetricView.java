package com.navercorp.pinpoint.metric.collector.view;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;

import java.util.Objects;

public class SystemMetricView {
    private final String applicationName;
    private final SystemMetric metric;

    public SystemMetricView(String applicationName, SystemMetric metric) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.metric = Objects.requireNonNull(metric, "metric");
    }

    public String getApplicationName() {
        return applicationName;
    }

    @JsonUnwrapped
    public SystemMetric getMetric() {
        return metric;
    }
}
