package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.Map;
import java.util.Objects;

public class DefaultStatChartGroup<P extends Point> implements StatChartGroup<P> {
    private final TimeWindow timeWindow;
    private final Map<ChartType, Chart<P>> chart;

    public DefaultStatChartGroup(TimeWindow timeWindow, Map<ChartType, Chart<P>> chart) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.chart = Objects.requireNonNull(chart, "chart");
    }

    @Override
    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    @Override
    public Map<ChartType, Chart<P>> getCharts() {
        return chart;
    }
}
