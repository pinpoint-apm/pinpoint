package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DefaultStatChartGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup.ChartType;

public class ChartGroupBuilder<T, P extends Point> {

    private final TimeSeriesChartBuilder<P> builder;
    private final List<Map.Entry<ChartType, Function<T, P>>> pointFunctions = new ArrayList<>();

    public ChartGroupBuilder(Point.UncollectedPointCreator<P> uncollectedPoint) {
        Objects.requireNonNull(uncollectedPoint, "uncollectedPoint");
        this.builder = new TimeSeriesChartBuilder<>(uncollectedPoint);
    }

    public StatChartGroup<P> build(TimeWindow timeWindow, List<T> appPointList) {
        Objects.requireNonNull(timeWindow, "timeWindow");
        Objects.requireNonNull(appPointList, "appPointList");

        Map<ChartType, Chart<P>> chartTypeChartMap = build(timeWindow, appPointList, pointFunctions);
        return new DefaultStatChartGroup<>(timeWindow, chartTypeChartMap);
    }

    public Map<ChartType, Chart<P>> buildMap(TimeWindow timeWindow, List<T> appPointList) {
        Objects.requireNonNull(timeWindow, "timeWindow");
        Objects.requireNonNull(appPointList, "appPointList");

        return build(timeWindow, appPointList, pointFunctions);
    }


    private Map<ChartType, Chart<P>> build(TimeWindow timeWindow, List<T> cpuLoadList, List<Map.Entry<ChartType, Function<T, P>>> entries) {
        Objects.requireNonNull(entries, "entries");

        Map.Entry<ChartType, Chart<P>>[] charts = new Map.Entry[entries.size()];
        int i = 0;
        for (Map.Entry<ChartType, Function<T, P>> entry : entries) {
            final ChartType chartType = entry.getKey();
            final Function<T, P> pointFunction = entry.getValue();

            Chart<P> chart = builder.build(timeWindow, cpuLoadList, pointFunction);

            charts[i++] = Map.entry(chartType, chart);
        }
        return Map.ofEntries(charts);
    }

    public void addPointFunction(ChartType chartType, Function<T, P> function) {
        Objects.requireNonNull(chartType, "chartType");
        Objects.requireNonNull(function, "function");

        this.pointFunctions.add(Map.entry(chartType, function));
    }

}