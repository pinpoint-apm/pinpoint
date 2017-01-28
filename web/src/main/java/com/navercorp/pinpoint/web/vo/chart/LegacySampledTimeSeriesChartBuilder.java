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

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;

/**
 * @author hyungil.jeong
 */
@Deprecated
public class LegacySampledTimeSeriesChartBuilder<D extends DataPoint<Long, Y>, Y extends Number> extends LegacySampledChartBuilder<D, Long, Y> {

    private final TimeWindow timeWindow;

    public LegacySampledTimeSeriesChartBuilder(DownSampler<Y> downSampler, Integer avgNumDecimals, TimeWindow timeWindow) {
        super(downSampler, avgNumDecimals);
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
    }

    @Override
    protected final List<Point<Long, Y>> makePoints(List<D> dataPoints) {
        List<List<D>> timeSlots = createTimeSlots(dataPoints);
        List<Point<Long, Y>> sampledPoints = new ArrayList<>(timeSlots.size());
        int index = 0;
        for (Long timestamp : this.timeWindow) {
            List<D> samples = timeSlots.get(index);
            if (samples.isEmpty()) {
                sampledPoints.add(new UncollectedPoint<>(timestamp, this.defaultValue));
            } else {
                sampledPoints.add(sampleDataPoints(timestamp, samples));
            }
            ++index;
        }
        return sampledPoints;
    }

    protected Point<Long, Y> sampleDataPoints(long timestamp, List<D> samples) {
        List<Y> values = new ArrayList<>(samples.size());
        for (D sample : samples) {
            values.add(sample.getYVal());
        }
        return new Point<>(timestamp, sampleMin(values), sampleMax(values), sampleAvg(values), sampleSum(values));
    }

    private List<List<D>> createTimeSlots(List<D> dataPoints) {
        int numTimeSlots = (int) this.timeWindow.getWindowRangeCount();
        List<List<D>> timeSlots = new ArrayList<>(numTimeSlots);
        for (int i = 0; i < numTimeSlots; ++i) {
            timeSlots.add(new ArrayList<D>());
        }
        for (D dataPoint : dataPoints) {
            long timestamp = dataPoint.getXVal();
            int index = this.timeWindow.getWindowIndex(timestamp);
            if (index >= 0 && index < timeSlots.size()) {
                List<D> timeSlot = timeSlots.get(index);
                timeSlot.add(dataPoint);
            }
        }
        return timeSlots;
    }
}
