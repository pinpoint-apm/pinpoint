package com.navercorp.pinpoint.otlp.web.vo;

import com.navercorp.pinpoint.common.server.util.time.Range;

import java.util.Objects;

public class OtlpMetricDetailsQueryParam {
    private final String serviceId;
    private final String applicationId;
    private final String agentId;
    private final String metricGroupName;
    private final String metricName;
    private String fieldName;
    private String rawTags;

    public OtlpMetricDetailsQueryParam(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String rawTags) {
        this.serviceId = serviceId;
        this.applicationId = Objects.requireNonNull(applicationId, "applicationId");;
        this.agentId = agentId;
        this.metricGroupName = Objects.requireNonNull(metricGroupName, "metricGroupName");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldName = "";
        this.rawTags = rawTags;

    }

    // TODO: check if this is needed
    public OtlpMetricDetailsQueryParam(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String fieldName, String rawTags) {
        this.serviceId = serviceId;
        this.applicationId = Objects.requireNonNull(applicationId, "applicationId");;
        this.agentId = agentId;
        this.metricGroupName = Objects.requireNonNull(metricGroupName, "metricGroupName");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldName = fieldName;
        this.rawTags = rawTags;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getMetricGroupName() {
        return metricGroupName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getRawTags() {
        return rawTags;
    }
}
