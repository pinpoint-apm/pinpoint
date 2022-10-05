package com.navercorp.pinpoint.metric.web.model;

import java.util.Objects;

public class MetricInfo {

    private final String metricDefinitionId;
    private final boolean isTagGroup;

    public MetricInfo(String metricDefinitionId, boolean isTagGroups) {
        this.metricDefinitionId = Objects.requireNonNull(metricDefinitionId, "metricDefinitionId");
        this.isTagGroup = isTagGroups;
    }

    public String getMetricDefinitionId() {
        return metricDefinitionId;
    }

    public boolean isTagGroup() {
        return isTagGroup;
    }
}
