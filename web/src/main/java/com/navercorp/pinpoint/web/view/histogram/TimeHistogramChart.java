package com.navercorp.pinpoint.web.view.histogram;

import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueGroupView;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;

import java.util.List;
import java.util.Objects;

public class TimeHistogramChart implements TimeSeriesView {
    private final String title;
    private final List<Long> timestamp;
    private final List<TimeSeriesValueGroupView> metricValueGroups;

    public TimeHistogramChart(String title, List<Long> timestamp, List<TimeSeriesValueGroupView> metricValueGroups) {
        this.title = Objects.requireNonNull(title, "title");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.metricValueGroups = Objects.requireNonNull(metricValueGroups, "metricValueGroups");
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<Long> getTimestamp() {
        return timestamp;
    }

    @Override
    public List<TimeSeriesValueGroupView> getMetricValueGroups() {
        return metricValueGroups;
    }
}
