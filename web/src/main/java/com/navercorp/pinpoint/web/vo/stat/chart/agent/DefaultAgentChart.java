package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Objects;

public class DefaultAgentChart<T, P extends Number> implements StatChart<AgentStatPoint<P>> {
    private final TimeWindow timeWindow;
    private final List<T> statList;
    private final ChartGroupBuilder<T, AgentStatPoint<P>> builder;

    public DefaultAgentChart(TimeWindow timeWindow, List<T> statList, ChartGroupBuilder<T, AgentStatPoint<P>> builder) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.statList = Objects.requireNonNull(statList, "statList");
        this.builder = Objects.requireNonNull(builder, "builder");
    }

    @Override
    public StatChartGroup<AgentStatPoint<P>> getCharts() {
        return builder.build(timeWindow, statList);
    }

}
