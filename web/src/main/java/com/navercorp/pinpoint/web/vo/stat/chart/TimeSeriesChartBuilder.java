/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.Point.UncollectedPointCreater;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class TimeSeriesChartBuilder {

    private final TimeWindow timeWindow;
    private final List<Point> pointList;

    public TimeSeriesChartBuilder(TimeWindow timeWindow, UncollectedPointCreater uncollectedPointCreater) {
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        int numTimeslots = (int) this.timeWindow.getWindowRangeCount();
        this.pointList = new ArrayList<>(numTimeslots);
        for (long timestamp : this.timeWindow) {
            this.pointList.add(uncollectedPointCreater.createUnCollectedPoint(timestamp));
        }
    }

    public Chart build(List<Point> pointList) {
        for (Point point : pointList) {
            int timeslotIndex = this.timeWindow.getWindowIndex(point.getxVal());
            if (timeslotIndex < 0 || timeslotIndex >= timeWindow.getWindowRangeCount()) {
                continue;
            }
            this.pointList.set(timeslotIndex, point);
        }
        return new Chart(this.pointList);
    }

}
