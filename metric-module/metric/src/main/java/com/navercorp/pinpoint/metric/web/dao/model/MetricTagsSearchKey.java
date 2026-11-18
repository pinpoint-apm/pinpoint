package com.navercorp.pinpoint.metric.web.dao.model;

import java.util.Objects;

public class MetricTagsSearchKey {
    private final String tenantId;
    private final String serviceName;
    private final String hostGroupName;
    private final String hostName;
    private final String metricName;

    public MetricTagsSearchKey(String tenantId, String serviceName, String hostGroupName, String hostName, String metricName) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.hostGroupName = Objects.requireNonNull(hostGroupName, "hostGroupName");
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getServiceName() {
        return serviceName;
    }
}
