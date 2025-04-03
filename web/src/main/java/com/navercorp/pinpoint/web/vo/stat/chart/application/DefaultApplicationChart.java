package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Objects;

public class DefaultApplicationChart<T> implements StatChart<ApplicationStatPoint> {
    private final TimeWindow timeWindow;
    private final List<T> statList;
    private final ChartGroupBuilder<T, ApplicationStatPoint> builder;

    public DefaultApplicationChart(TimeWindow timeWindow, List<T> statList, ChartGroupBuilder<T, ApplicationStatPoint> builder) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.statList = Objects.requireNonNull(statList, "statList");
        this.builder = Objects.requireNonNull(builder, "builder");
    }

    @Override
    public StatChartGroup<ApplicationStatPoint> getCharts() {
        return builder.build(timeWindow, statList);
    }

}
