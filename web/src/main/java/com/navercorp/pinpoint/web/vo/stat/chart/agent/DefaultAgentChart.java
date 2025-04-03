package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Objects;

public class DefaultAgentChart<T> implements StatChart<DataPoint<Double>> {
    private final TimeWindow timeWindow;
    private final List<T> statList;
    private final ChartGroupBuilder<T, DataPoint<Double>> builder;

    public DefaultAgentChart(TimeWindow timeWindow, List<T> statList, ChartGroupBuilder<T, DataPoint<Double>> builder) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.statList = Objects.requireNonNull(statList, "statList");
        this.builder = Objects.requireNonNull(builder, "builder");
    }

    @Override
    public StatChartGroup<DataPoint<Double>> getCharts() {
        return builder.build(timeWindow, statList);
    }

}
