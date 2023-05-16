package com.navercorp.pinpoint.web.vo.stat.chart;

import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

public class InspectorValueGroup {

    private final String groupName;
    private final List<InspectorValue<?>> inspectorValueList;

    public InspectorValueGroup(List<InspectorValue<?>> inspectorValueList, String groupName) {
        Assert.hasLength(groupName, "groupName must not be empty");
        this.groupName = groupName;
        this.inspectorValueList = Objects.requireNonNull(inspectorValueList, "metricValueList");
    }

    public List<InspectorValue<?>> getMetricValueList() {
        return inspectorValueList;
    }

    public String getGroupName() {
        return groupName;
    }
}
