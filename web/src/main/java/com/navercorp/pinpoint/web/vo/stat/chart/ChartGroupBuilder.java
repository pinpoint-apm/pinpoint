package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DefaultStatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup.ChartType;

public class ChartGroupBuilder<T, P extends Point> {

    private final TimeSeriesChartBuilder<P> builder;
    private final List<ChartTransform<T, P>> pointFunctions = new ArrayList<>();

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


    private Map<ChartType, Chart<P>> build(TimeWindow timeWindow, List<T> cpuLoadList, List<ChartTransform<T, P>> entries) {
        Objects.requireNonNull(entries, "entries");

        Map<ChartType, Chart<P>> charts = new HashMap<>(entries.size());
        for (ChartTransform<T, P> transform : entries) {
            final ChartType chartType = transform.chartType();
            final Function<T, P> pointFunction = transform.function();

            Chart<P> chart = builder.build(timeWindow, cpuLoadList, pointFunction);
            Chart<P> exist = charts.put(chartType, chart);
            if (exist != null) {
                throw new IllegalStateException("duplicated chartType:" + chartType);
            }
        }
        return charts;
    }

    public void addPointFunction(ChartType chartType, Function<T, P> function) {
        Objects.requireNonNull(chartType, "chartType");
        Objects.requireNonNull(function, "function");

        this.pointFunctions.add(new ChartTransform<>(chartType, function));
    }

    record ChartTransform<T, P>(ChartType chartType, Function<T, P> function) {
        public ChartTransform {
            Objects.requireNonNull(chartType, "chartType");
            Objects.requireNonNull(function, "function");
        }
    }
}