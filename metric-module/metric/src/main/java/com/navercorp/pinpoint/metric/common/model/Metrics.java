package com.navercorp.pinpoint.metric.common.model;


import jakarta.validation.Valid;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Metrics implements Iterable<DoubleMetric> {
    private final String tenantId;
    private final String hostGroupName;
    private final String hostName;

    @Valid
    private final List<DoubleMetric> metrics;

    public Metrics(String tenantId, String hostGroupName, String hostName, List<DoubleMetric> metrics) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.hostGroupName = Objects.requireNonNull(hostGroupName, "hostGroupName");
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public List<DoubleMetric> getMetrics() {
        return metrics;
    }

    @Override
    public Iterator<DoubleMetric> iterator() {
        return metrics.iterator();
    }

    public int size() {
        return metrics.size();
    }

}
