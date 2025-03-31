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

/**
 * @author minwoo-jung
 */
public class AvgMinMaxMetricPoint implements Point {

    private final long xValue;

    private final double avgValue;

    private final double minValue;

    private final double maxValue;

    public AvgMinMaxMetricPoint(long xValue, double avgValue, double minValue, double maxValue) {
        this.xValue = xValue;
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public double getAvgValue() {
        return avgValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AvgMinMaxMetricPoint that = (AvgMinMaxMetricPoint) o;
        return xValue == that.xValue && Double.compare(avgValue, that.avgValue) == 0 && Double.compare(minValue, that.minValue) == 0 && Double.compare(maxValue, that.maxValue) == 0;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(xValue);
        result = 31 * result + Double.hashCode(avgValue);
        result = 31 * result + Double.hashCode(minValue);
        result = 31 * result + Double.hashCode(maxValue);
        return result;
    }

    @Override
    public String toString() {
        return "AvgMinMaxMetricPoint{" +
                "xValue=" + xValue +
                ", avgValue=" + avgValue +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }

    @Override
    public long getXVal() {
        return this.xValue;
    }
}
