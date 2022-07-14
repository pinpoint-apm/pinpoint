package com.navercorp.pinpoint.web.view.timeseries;

import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

public class TimeSeriesValueGroup {

    private final String groupName;
    private final List<TimeSeriesValue> inspectorValueList;

    public TimeSeriesValueGroup(List<TimeSeriesValue> inspectorValueList, String groupName) {
        Assert.hasLength(groupName, "groupName must not be empty");
        this.groupName = groupName;
        this.inspectorValueList = Objects.requireNonNull(inspectorValueList, "metricValueList");
    }

    public List<TimeSeriesValue> getMetricValueList() {
        return inspectorValueList;
    }

    public String getGroupName() {
        return groupName;
    }
}
