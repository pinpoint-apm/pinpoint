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

import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class AvgMinMetricPoint <Y extends Number> implements Point {

    private final long xValue;

    private final Y avgValue;

    private final Y minValue;

    public AvgMinMetricPoint(long xValue, Y avgValue, Y minValue) {
        this.xValue = xValue;
        this.avgValue = avgValue;
        this.minValue = minValue;
    }

    public Y getAvgValue() {
        return avgValue;
    }

    public Y getMinValue() {
        return minValue;
    }

    @Override
    public long getXVal() {
        return xValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvgMinMetricPoint<?> that = (AvgMinMetricPoint<?>) o;
        return xValue == that.xValue && Objects.equals(avgValue, that.avgValue) && Objects.equals(minValue, that.minValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xValue, avgValue, minValue);
    }
}
