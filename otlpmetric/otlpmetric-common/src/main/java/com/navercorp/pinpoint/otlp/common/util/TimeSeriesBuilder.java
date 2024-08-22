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

/**
 * @author minwoo-jung
 */
public class TimeSeriesBuilder<T extends Number> {

    private final TimeWindow timeWindow;
    private final UncollectedDataCreator<T> uncollectedDataCreator;

    public TimeSeriesBuilder(TimeWindow timeWindow, UncollectedDataCreator<T> uncollectedDataCreator) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.uncollectedDataCreator = Objects.requireNonNull(uncollectedDataCreator, "uncollectedDataCreator");
    }

    public List<MetricPoint<T>> build(List<MetricPoint<T>> metricDataList) {
        List<MetricPoint<T>> filledMetricPointList = createInitialPoints();

        for (MetricPoint<T> metricPoint : metricDataList) {
            int timeslotIndex = this.timeWindow.getWindowIndex(metricPoint.getXVal());
            if (timeslotIndex < 0 || timeslotIndex >= timeWindow.getWindowRangeCount()) {
                continue;
            }
            filledMetricPointList.set(timeslotIndex, metricPoint);
        }

        return filledMetricPointList;
    }

    private List<MetricPoint<T>> createInitialPoints() {
        int numTimeslots = (int) this.timeWindow.getWindowRangeCount();
        List<MetricPoint<T>> pointList = new ArrayList<>(numTimeslots);

        for (long timestamp : this.timeWindow) {
            pointList.add(uncollectedDataCreator.createUnCollectedPoint(timestamp));
        }

        return pointList;
    }

}