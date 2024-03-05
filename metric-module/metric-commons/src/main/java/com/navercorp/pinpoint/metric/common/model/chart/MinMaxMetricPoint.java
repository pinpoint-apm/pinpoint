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
public class MinMaxMetricPoint <Y extends Number> implements Point {

    private final long xValue;

    private final Y minValue;

    private final Y maxValue;

    public MinMaxMetricPoint(long xValue, Y minValue, Y maxValue) {
        this.xValue = xValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Y getMinValue() {
        return minValue;
    }

    public Y getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinMaxMetricPoint<?> that = (MinMaxMetricPoint<?>) o;
        return xValue == that.xValue && Objects.equals(minValue, that.minValue) && Objects.equals(maxValue, that.maxValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xValue, minValue, maxValue);
    }

    @Override
    public String toString() {
        return "MinMaxMetricPoint{" +
                "xValue=" + xValue +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }

    @Override
    public long getXVal() {
        return this.xValue;
    }
}
