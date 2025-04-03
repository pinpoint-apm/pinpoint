/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.common.model.chart;

import com.navercorp.pinpoint.common.timeseries.point.Point;

/**
 * @author minwoo-jung
 */
public class AvgMinMetricPoint implements Point {

    private final long timestamp;

    private final double avgValue;

    private final double minValue;

    public AvgMinMetricPoint(long timestamp, double avgValue, double minValue) {
        this.timestamp = timestamp;
        this.avgValue = avgValue;
        this.minValue = minValue;
    }

    public double getAvgValue() {
        return avgValue;
    }

    public double getMinValue() {
        return minValue;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AvgMinMetricPoint that = (AvgMinMetricPoint) o;
        return timestamp == that.timestamp && Double.compare(avgValue, that.avgValue) == 0 && Double.compare(minValue, that.minValue) == 0;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(timestamp);
        result = 31 * result + Double.hashCode(avgValue);
        result = 31 * result + Double.hashCode(minValue);
        return result;
    }
}
