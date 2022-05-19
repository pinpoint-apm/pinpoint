package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.metric.common.model.Tag;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InspectorValueGroupBuilder<P extends Point> {

    private final Point.UncollectedPointCreator<P> uncollectedPointCreator;
    private final Map<String, Function<P, ?>> functions = new HashMap<>();

    public InspectorValueGroupBuilder(Point.UncollectedPointCreator<P> uncollectedPointCreator) {
        this.uncollectedPointCreator = Objects.requireNonNull(uncollectedPointCreator, "uncollectedPointCreator");
    }

    public InspectorValueGroup build(TimeWindow timeWindow, String groupName, List<P> sampledPoints) {
        List<P> points = createInitialPoints(timeWindow);
        for (P sampledPoint : sampledPoints) {
            int timeslotIndex = timeWindow.getWindowIndex(sampledPoint.getXVal());
            if (timeslotIndex < 0 || timeslotIndex >= timeWindow.getWindowRangeCount()) {
                continue;
            }
            points.set(timeslotIndex, sampledPoint);
        }

        List<InspectorValue<?>> inspectorValueList = getInspectorValueList(points);
        return new InspectorValueGroup(inspectorValueList, groupName);
    }

    private List<InspectorValue<?>> getInspectorValueList(List<P> points) {
        List<InspectorValue<?>> inspectorValueList = new ArrayList<>();
        for (Map.Entry<String, Function<P, ?>> e : functions.entrySet()) {
            String fieldName = e.getKey();
            Function<P, ?> function = e.getValue();
            InspectorValue<?> inspectorValue = getMetricValue(points, fieldName, function);
            inspectorValueList.add(inspectorValue);
        }
        return inspectorValueList;
    }

    private InspectorValue<?> getMetricValue(List<P> statPoints, String fieldName, Function<P, ?> function) {
        List<?> valueList = statPoints.stream()
                .map(function)
                .collect(Collectors.toList());
        List<Tag> emptyTagList = Collections.emptyList();
        InspectorValue<?> inspectorValue = new InspectorValue<>(fieldName, emptyTagList, valueList);
        return inspectorValue;
    }

    public void addValueFunction(String fieldName, Function<P, ?> function) {
        functions.put(fieldName, function);
    }

    private List<P> createInitialPoints(TimeWindow timeWindow) {
        int numTimeslots = (int) timeWindow.getWindowRangeCount();
        List<P> points = new ArrayList<>(numTimeslots);
        for (long timestamp : timeWindow) {
            points.add(uncollectedPointCreator.createUnCollectedPoint(timestamp));
        }
        return points;
    }
}