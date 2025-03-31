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
 * @author Hyunjoon Cho
 */
public class DoubleSystemMetricPoint extends AbstractSystemMetricPoint<Double> {

    private final double y;

    public DoubleSystemMetricPoint(long x, double y) {
        super(x);
        this.y = y;
    }


    @Override
    public Double getYVal() {
        return getRawY();
    }

    public double getRawY() {
        return y;
    }

    public static double getRawY(SystemMetricPoint<Double> point) {
        if (point instanceof DoubleSystemMetricPoint p) {
            return p.getRawY();
        }
        throw new IllegalArgumentException("Unsupported SystemMetricPoint");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DoubleSystemMetricPoint that = (DoubleSystemMetricPoint) o;
        return Double.compare(y, that.y) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Double.hashCode(y);
        return result;
    }

    @Override
    public String toString() {
        return "SystemMetricPoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
