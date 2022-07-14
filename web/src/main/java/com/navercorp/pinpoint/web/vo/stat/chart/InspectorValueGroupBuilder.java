package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.metric.common.model.Tag;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.view.timeseries.TimeSeriesValue;
import com.navercorp.pinpoint.web.view.timeseries.TimeSeriesValueGroup;
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

    public TimeSeriesValueGroup build(TimeWindow timeWindow, String groupName, List<P> sampledPoints) {
        List<P> points = createInitialPoints(timeWindow);
        for (P sampledPoint : sampledPoints) {
            int timeslotIndex = timeWindow.getWindowIndex(sampledPoint.getXVal());
            if (timeslotIndex < 0 || timeslotIndex >= timeWindow.getWindowRangeCount()) {
                continue;
            }
            points.set(timeslotIndex, sampledPoint);
        }

        List<TimeSeriesValue> timeSeriesValueList = getInspectorValueList(points);
        return new TimeSeriesValueGroup(timeSeriesValueList, groupName);
    }

    private List<TimeSeriesValue> getInspectorValueList(List<P> points) {
        List<TimeSeriesValue> timeSeriesValueList = new ArrayList<>();
        for (Map.Entry<String, Function<P, ?>> e : functions.entrySet()) {
            String fieldName = e.getKey();
            Function<P, ?> function = e.getValue();
            TimeSeriesValue timeSeriesValue = getMetricValue(points, fieldName, function);
            timeSeriesValueList.add(timeSeriesValue);
        }
        return timeSeriesValueList;
    }

    private TimeSeriesValue getMetricValue(List<P> statPoints, String fieldName, Function<P, ?> function) {
        List<?> valueList = statPoints.stream()
                .map(function)
                .collect(Collectors.toList());
        List<Tag> emptyTagList = Collections.emptyList();
        TimeSeriesValue timeSeriesValue = new TimeSeriesValue(fieldName, emptyTagList, valueList);
        return timeSeriesValue;
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