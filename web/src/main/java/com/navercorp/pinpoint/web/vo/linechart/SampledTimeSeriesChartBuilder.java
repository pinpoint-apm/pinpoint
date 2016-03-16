/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.linechart;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.linechart.Chart.Point;
import com.navercorp.pinpoint.web.vo.linechart.Chart.Points;

/**
 * @author hyungil.jeong
 */
public class SampledTimeSeriesChartBuilder<Y extends Number> extends SampledChartBuilder<Long, Y> {

    private final TimeWindow timeWindow;
    private final List<List<Y>> timeslots;

    public SampledTimeSeriesChartBuilder(DownSampler<Y> downSampler, TimeWindow timeWindow) {
        super(downSampler);
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        int numTimeslots = (int) this.timeWindow.getWindowRangeCount();
        this.timeslots = new ArrayList<>(numTimeslots);
        initializeTimeslots(numTimeslots);
    }

    private void initializeTimeslots(int numTimeslots) {
        for (int i = 0; i < numTimeslots; ++i) {
            this.timeslots.add(new ArrayList<Y>());
        }
    }

    @Override
    public Points makePoints(List<DataPoint<Long, Y>> dataPoints) {
        Points points = new Points();
        allocateDataPoints(dataPoints);
        int timeSlotIndex = 0;
        for (Long timestamp : this.timeWindow) {
            List<Y> samples = this.timeslots.get(timeSlotIndex);
            Point point = new Point(timestamp, sampleMin(samples), sampleMax(samples), sampleAvg(samples));
            points.addPoint(point);
            ++timeSlotIndex;
        }
        return points;
    }

    private void allocateDataPoints(List<DataPoint<Long, Y>> dataPoints) {
        for (DataPoint<Long, Y> dataPoint : dataPoints) {
            int timeslotIndex = this.timeWindow.getWindowIndex(dataPoint.getxVal());
            if (isValidIndex(timeslotIndex)) {
                List<Y> timeSlottedDataPoints = this.timeslots.get(timeslotIndex);
                timeSlottedDataPoints.add(dataPoint.getyVal());
            }
        }
    }

    private boolean isValidIndex(int timeslot) {
        return timeslot >= 0 && timeslot < this.timeslots.size();
    }

}
