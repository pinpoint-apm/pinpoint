package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Objects;

public class DefaultApplicationChart<T, P extends Number> implements StatChart<ApplicationStatPoint<P>> {
    private final TimeWindow timeWindow;
    private final List<T> statList;
    private final ChartGroupBuilder<T, ApplicationStatPoint<P>> builder;

    public DefaultApplicationChart(TimeWindow timeWindow, List<T> statList, ChartGroupBuilder<T, ApplicationStatPoint<P>> builder) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.statList = Objects.requireNonNull(statList, "statList");
        this.builder = Objects.requireNonNull(builder, "builder");
    }

    @Override
    public StatChartGroup<ApplicationStatPoint<P>> getCharts() {
        return builder.build(timeWindow, statList);
    }

}
