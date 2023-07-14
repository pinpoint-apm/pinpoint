package com.navercorp.pinpoint.web.view.histogram;

import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueGroupView;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueView;

import java.util.List;
import java.util.Objects;

public class TimeHistogramValueGroupView implements TimeSeriesValueGroupView {
    private final String groupName;
    private final String unit;
    private final String chartType;
    private final List<TimeSeriesValueView> metricValues;

    public TimeHistogramValueGroupView(String groupName, String unit, String chartType, List<TimeSeriesValueView> metricValues) {
        this.groupName = Objects.requireNonNull(groupName, "groupName");
        this.chartType = Objects.requireNonNull(chartType, "chartType");

        this.metricValues = Objects.requireNonNull(metricValues, "metricValues");
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getChartType() {
        return chartType;
    }

    @Override
    public List<TimeSeriesValueView> getMetricValues() {
        return metricValues;
    }

    @Override
    public String getUnit() {
        return unit;
    }
}
