package com.navercorp.pinpoint.metric.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Metrics {

    private final List<SystemMetric> metrics;

    public Metrics(@JsonProperty("metrics") List<SystemMetric> metrics) {
        this.metrics = metrics;
    }

    public List<SystemMetric> getMetrics() {
        return metrics;
    }

}
