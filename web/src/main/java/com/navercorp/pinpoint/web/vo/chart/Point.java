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

/**
 * @author HyunGil Jeong
 */
public class Point<X extends Number, Y extends Number> {
    private final X xVal;
    private final Y minYVal;
    private final Y maxYVal;
    private final double avgYVal;

    public Point(X xVal, Y yVal) {
        this(xVal, yVal, yVal, yVal.doubleValue());
    }

    public Point(X xVal, Y minYVal, Y maxYVal, double avgYVal) {
        this.xVal = xVal;
        this.minYVal = minYVal;
        this.maxYVal = maxYVal;
        this.avgYVal = avgYVal;
    }

    public X getxVal() {
        return xVal;
    }

    public Y getMinYVal() {
        return minYVal;
    }

    public Y getMaxYVal() {
        return maxYVal;
    }

    public double getAvgYVal() {
        return avgYVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point<?, ?> point = (Point<?, ?>) o;

        if (Double.compare(point.avgYVal, avgYVal) != 0) return false;
        if (xVal != null ? !xVal.equals(point.xVal) : point.xVal != null) return false;
        if (minYVal != null ? !minYVal.equals(point.minYVal) : point.minYVal != null) return false;
        return maxYVal != null ? maxYVal.equals(point.maxYVal) : point.maxYVal == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = xVal != null ? xVal.hashCode() : 0;
        result = 31 * result + (minYVal != null ? minYVal.hashCode() : 0);
        result = 31 * result + (maxYVal != null ? maxYVal.hashCode() : 0);
        temp = Double.doubleToLongBits(avgYVal);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Point{" +
                "xVal=" + xVal +
                ", minYVal=" + minYVal +
                ", maxYVal=" + maxYVal +
                ", avgYVal=" + avgYVal +
                '}';
    }
}
