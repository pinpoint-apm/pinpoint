/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.vo.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class TimeSeriesChartBuilder<Y extends Number> {

    private final TimeWindow timeWindow;
    private final List<Point<Long, Y>> points;

    public TimeSeriesChartBuilder(TimeWindow timeWindow, Y uncollectedValue) {
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        int numTimeslots = (int) this.timeWindow.getWindowRangeCount();
        this.points = new ArrayList<>(numTimeslots);
        for (long timestamp : this.timeWindow) {
            this.points.add(new UncollectedPoint<>(timestamp, uncollectedValue));
        }
    }

    public Chart<Long, Y> build(List<Point<Long, Y>> sampledPoints) {
        for (Point<Long, Y> sampledPoint : sampledPoints) {
            int timeslotIndex = this.timeWindow.getWindowIndex(sampledPoint.getxVal());
            if (timeslotIndex < 0 || timeslotIndex >= timeWindow.getWindowRangeCount()) {
                continue;
            }
            this.points.set(timeslotIndex, sampledPoint);
        }
        return new Chart<>(this.points);
    }
}
