/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.web.util.metric;

import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class TimeSeriesBuilder<T extends Number> {

    private final TimeWindow timeWindow;
    private final UncollectedDataCreator<T> uncollectedDataCreator;

    public TimeSeriesBuilder(TimeWindow timeWindow, UncollectedDataCreator<T> uncollectedDataCreator) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.uncollectedDataCreator = Objects.requireNonNull(uncollectedDataCreator, "uncollectedDataCreator");
    }


    public List<SystemMetricPoint<T>> build(List<SystemMetricPoint<T>> systemMetricDataList) {
        List<SystemMetricPoint<T>> filledSystemMetricPointList = createInitialPoints();

        for (SystemMetricPoint<T> systemMetricPoint : systemMetricDataList) {
            int timeslotIndex = this.timeWindow.getWindowIndex(systemMetricPoint.getXVal());
            if (timeslotIndex < 0 || timeslotIndex >= timeWindow.getWindowRangeCount()) {
                continue;
            }
            filledSystemMetricPointList.set(timeslotIndex, systemMetricPoint);
        }

        return filledSystemMetricPointList;
    }

    private List<SystemMetricPoint<T>> createInitialPoints() {
        int numTimeslots = (int) this.timeWindow.getWindowRangeCount();
        List<SystemMetricPoint<T>> pointList = new ArrayList<>(numTimeslots);

        for (long timestamp : this.timeWindow) {
            pointList.add(uncollectedDataCreator.createUnCollectedPoint(timestamp));
        }

        return pointList;
    }
}


