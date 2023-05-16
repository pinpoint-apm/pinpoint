package com.navercorp.pinpoint.web.vo.stat.chart;

import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

public class InspectorData {
    private final String title;
    private final String unit;
    private final List<Long> timeStampList;
    private final List<InspectorValueGroup> inspectorValueGroupList;

    public InspectorData(String title, String unit, List<Long> timeStampList, List<InspectorValueGroup> inspectorValueGroupList) {
        Assert.hasLength(title, "title must not be empty");
        Assert.hasLength(unit, "unit must not be empty");
        this.title = title;
        this.unit = unit;
        this.timeStampList = Objects.requireNonNull(timeStampList, "timeStampList");
        this.inspectorValueGroupList = Objects.requireNonNull(inspectorValueGroupList, "inspectorValueGroupList");
    }

    public String getUnit() {
        return unit;
    }

    public String getTitle() {
        return title;
    }

    public List<Long> getTimeStampList() {
        return timeStampList;
    }

    public List<InspectorValueGroup> getMetricValueGroupList() {
        return inspectorValueGroupList;
    }

}
