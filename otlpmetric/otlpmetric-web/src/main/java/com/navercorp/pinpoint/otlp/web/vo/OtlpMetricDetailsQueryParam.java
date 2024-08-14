package com.navercorp.pinpoint.otlp.web.vo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OtlpMetricDetailsQueryParam {
    private final String serviceId;
    private final String applicationId;
    private final String agentId;
    private final String metricGroupName;
    private final String metricName;
    private List<String> fieldNameList;
    private String rawTags;

    @Deprecated
    public OtlpMetricDetailsQueryParam(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String rawTags) {
        this.serviceId = serviceId;
        this.applicationId = Objects.requireNonNull(applicationId, "applicationId");
        this.agentId = agentId;
        this.metricGroupName = Objects.requireNonNull(metricGroupName, "metricGroupName");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldNameList = Collections.emptyList();
        this.rawTags = rawTags;

    }

    // TODO: check if this is needed
    public OtlpMetricDetailsQueryParam(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, List<String> fieldNameList, String rawTags) {
        this.serviceId = serviceId;
        this.applicationId = Objects.requireNonNull(applicationId, "applicationId");
        this.agentId = agentId;
        this.metricGroupName = Objects.requireNonNull(metricGroupName, "metricGroupName");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldNameList = fieldNameList;
        this.rawTags = rawTags;
    }

    public List<String> getFieldNameList() {
        return fieldNameList;
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
