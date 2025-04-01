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

package com.navercorp.pinpoint.otlp.common.model;

/**
 * @author minwoo-jung
 */
public class DoubleMetricPoint implements MetricPoint<Double> {
    private final long x;
    private final double y;

    public DoubleMetricPoint(long x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public long getXVal() {
        return x;
    }

    @Override
    public Double getYVal() {
        return y;
    }

    public double getRawYVal() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        DoubleMetricPoint that = (DoubleMetricPoint) o;
        return x == that.x && Double.compare(y, that.y) == 0;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        return result;
    }

    @Override
    public String toString() {
        return "MetricPoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

