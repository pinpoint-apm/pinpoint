package com.navercorp.pinpoint.web.view.timeseries;

import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

public class TimeSeriesData {
    private final String title;
    private final String unit;
    private final List<Long> timeStampList;
    private final List<TimeSeriesValueGroup> timeSeriesValueGroupList;

    public TimeSeriesData(String title, String unit, List<Long> timeStampList, List<TimeSeriesValueGroup> timeSeriesValueGroupList) {
        Assert.hasLength(title, "title must not be empty");
        Assert.hasLength(unit, "unit must not be empty");
        this.title = title;
        this.unit = unit;
        this.timeStampList = Objects.requireNonNull(timeStampList, "timeStampList");
        this.timeSeriesValueGroupList = Objects.requireNonNull(timeSeriesValueGroupList, "inspectorValueGroupList");
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

    public List<TimeSeriesValueGroup> getMetricValueGroupList() {
        return timeSeriesValueGroupList;
    }

}
