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

import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindows;

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

    public List<DataPoint<T>> build(LongFunction<DataPoint<T>> function, List<DataPoint<T>> metricDataList) {
        List<DataPoint<T>> filledMetricPointList = TimeWindows.createInitialPoints(timeWindow, function);
        final int windowRangeCount = timeWindow.getWindowRangeCount();
        for (DataPoint<T> metricPoint : metricDataList) {
            int timeslotIndex = this.timeWindow.getWindowIndex(metricPoint.getTimestamp());
            if (timeslotIndex < 0 || timeslotIndex >= windowRangeCount) {
                continue;
            }
            filledMetricPointList.set(timeslotIndex, metricPoint);
        }

        return filledMetricPointList;
    }

}