package com.navercorp.pinpoint.common.timeseries.window;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

public final class TimeWindows {
    private TimeWindows() {
    }

    public static <T> List<T> createInitialPoints(TimeWindow timeWindow, LongFunction<T> function) {
        final int numTimeslots = timeWindow.getWindowRangeCount();
        List<T> points = new ArrayList<>(numTimeslots);
        for (long timestamp : timeWindow) {
            points.add(function.apply(timestamp));
        }
        return points;
    }
}
