package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InspectorDataBuilder<T, P extends Point> {

    private final InspectorValueGroupBuilder<P> inspectorValueGroupBuilder;
    private final List<Map.Entry<StatChartGroup.ChartType, Function<T, P>>> pointFunctions = new ArrayList<>();
    private final String title;
    private final String unit;

    public InspectorDataBuilder(Point.UncollectedPointCreator<P> uncollectedPointCreator, String title, String unit) {
        inspectorValueGroupBuilder = new InspectorValueGroupBuilder<>(uncollectedPointCreator);
        this.title = Objects.requireNonNull(title, "title");
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    private InspectorData build(TimeWindow timeWindow, List<T> sampledPoints, List<Map.Entry<StatChartGroup.ChartType, Function<T, P>>> entries) {
        Objects.requireNonNull(entries, "entries");

        List<InspectorValueGroup> inspectorValueGroupList = new ArrayList<>(entries.size());
        for (Map.Entry<StatChartGroup.ChartType, Function<T, P>> entry : entries) {
            String groupName = entry.getKey().toString();
            Function<T, P> function = entry.getValue();
            List<P> points = sampledPoints.stream()
                    .map(function)
                    .collect(Collectors.toList());

            InspectorValueGroup inspectorValueGroup = inspectorValueGroupBuilder.build(timeWindow, groupName, points);
            inspectorValueGroupList.add(inspectorValueGroup);
        }
        List<Long> timestampList = getTimestampList(timeWindow);
        return new InspectorData(title, unit, timestampList, inspectorValueGroupList);
    }

    public InspectorData build(TimeWindow timeWindow, List<T> sampledPoints) {
        Objects.requireNonNull(timeWindow, "timeWindow");
        Objects.requireNonNull(sampledPoints, "sampledPointList");

        return build(timeWindow, sampledPoints, pointFunctions);
    }

    public void addPointFunction(StatChartGroup.ChartType inspectorValueType, Function<T, P> function) {
        Objects.requireNonNull(inspectorValueType, "inspectorValueType");
        Objects.requireNonNull(function, "function");

        this.pointFunctions.add(Map.entry(inspectorValueType, function));
    }

    public void addValueFunction(String FieldName, Function<P, ?> function) {
        Objects.requireNonNull(FieldName, "FieldName");
        Objects.requireNonNull(function, "function");

        this.inspectorValueGroupBuilder.addValueFunction(FieldName, function);
    }

    private List<Long> getTimestampList(TimeWindow timeWindow) {
        List<Long> timestampList = new ArrayList<>((int) timeWindow.getWindowSlotSize());
        for (Long timeStamp : timeWindow) {
            timestampList.add(timeStamp);
        }
        return timestampList;
    }
}