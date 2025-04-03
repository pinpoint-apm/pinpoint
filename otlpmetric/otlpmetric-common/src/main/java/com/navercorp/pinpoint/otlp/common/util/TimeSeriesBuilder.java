/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.common.util;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.otlp.common.model.MetricPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.LongFunction;

/**
 * @author minwoo-jung
 */
public class TimeSeriesBuilder<T extends Number> {

    private final TimeWindow timeWindow;

    public TimeSeriesBuilder(TimeWindow timeWindow) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
    }

    public List<MetricPoint<T>> build(LongFunction<MetricPoint<T>> function, List<MetricPoint<T>> metricDataList) {
        List<MetricPoint<T>> filledMetricPointList = createInitialPoints(function);
        final int windowRangeCount = timeWindow.getWindowRangeCount();
        for (MetricPoint<T> metricPoint : metricDataList) {
            int timeslotIndex = this.timeWindow.getWindowIndex(metricPoint.getTimestamp());
            if (timeslotIndex < 0 || timeslotIndex >= windowRangeCount) {
                continue;
            }
            filledMetricPointList.set(timeslotIndex, metricPoint);
        }

        return filledMetricPointList;
    }

    private List<MetricPoint<T>> createInitialPoints(LongFunction<MetricPoint<T>> function) {
        final int numTimeslots = this.timeWindow.getWindowRangeCount();
        List<MetricPoint<T>> pointList = new ArrayList<>(numTimeslots);

        for (long timestamp : this.timeWindow) {
            pointList.add(function.apply(timestamp));
        }

        return pointList;
    }

}