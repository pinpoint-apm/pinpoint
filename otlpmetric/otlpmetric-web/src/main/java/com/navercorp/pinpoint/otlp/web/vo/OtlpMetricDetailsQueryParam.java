package com.navercorp.pinpoint.otlp.web.vo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OtlpMetricDetailsQueryParam {
    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final String metricGroupName;
    private final String metricName;
    private List<String> fieldNameList;
    private List<String> tagGroupList;

    @Deprecated
    public OtlpMetricDetailsQueryParam(String serviceName, String applicationName, String agentId, String metricGroupName, String metricName, List<String> tagGroupList) {
        this.serviceName = serviceName;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = agentId;
        this.metricGroupName = Objects.requireNonNull(metricGroupName, "metricGroupName");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldNameList = Collections.emptyList();
        this.tagGroupList = tagGroupList;

    }

    // TODO: check if this is needed
    public OtlpMetricDetailsQueryParam(String serviceName, String applicationName, String agentId, String metricGroupName, String metricName, List<String> fieldNameList, List<String> tagGroupList) {
        this.serviceName = serviceName;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = agentId;
        this.metricGroupName = Objects.requireNonNull(metricGroupName, "metricGroupName");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldNameList = fieldNameList;
        this.tagGroupList = tagGroupList;
    }

    public List<String> getFieldNameList() {
        return fieldNameList;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
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

    public List<String> getTagGroupList() {
        return tagGroupList;
    }
}
