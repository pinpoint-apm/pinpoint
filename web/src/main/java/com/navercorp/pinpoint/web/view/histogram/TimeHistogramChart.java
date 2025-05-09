package com.navercorp.pinpoint.web.view.histogram;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueGroupView;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public class TimeHistogramChart implements TimeSeriesView {
    private final String title;
    @Nullable
    private final List<Long> timestamp;
    private final List<TimeSeriesValueGroupView> metricValueGroups;

    public TimeHistogramChart(String title, List<Long> timestamp, List<TimeSeriesValueGroupView> metricValueGroups) {
        this.title = Objects.requireNonNull(title, "title");
        this.timestamp = timestamp;
        this.metricValueGroups = Objects.requireNonNull(metricValueGroups, "metricValueGroups");
    }

    @Override
    public String getTitle() {
        return title;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public List<Long> getTimestamp() {
        return timestamp;
    }

    @Override
    public List<TimeSeriesValueGroupView> getMetricValueGroups() {
        return metricValueGroups;
    }
}
