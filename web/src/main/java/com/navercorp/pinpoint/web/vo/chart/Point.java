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
    private final Double avgYVal;
    private final Y sumYVal;

    public Point(X xVal, Y yVal) {
        this.xVal = xVal;
        this.minYVal = yVal;
        this.maxYVal = yVal;
        if (yVal == null) {
            this.avgYVal = null;
        } else {
            this.avgYVal = yVal.doubleValue();
        }
        this.sumYVal = yVal;
    }

    public Point(X xVal, Y minYVal, Y maxYVal, Double avgYVal, Y sumYVal) {
        this.xVal = xVal;
        this.minYVal = minYVal;
        this.maxYVal = maxYVal;
        this.avgYVal = avgYVal;
        this.sumYVal = sumYVal;
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

    public Double getAvgYVal() {
        return avgYVal;
    }

    public Y getSumYVal() {
        return sumYVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point<?, ?> point = (Point<?, ?>) o;

        if (xVal != null ? !xVal.equals(point.xVal) : point.xVal != null) return false;
        if (minYVal != null ? !minYVal.equals(point.minYVal) : point.minYVal != null) return false;
        if (maxYVal != null ? !maxYVal.equals(point.maxYVal) : point.maxYVal != null) return false;
        if (avgYVal != null ? !avgYVal.equals(point.avgYVal) : point.avgYVal != null) return false;
        return sumYVal != null ? sumYVal.equals(point.sumYVal) : point.sumYVal == null;
    }

    @Override
    public int hashCode() {
        int result = xVal != null ? xVal.hashCode() : 0;
        result = 31 * result + (minYVal != null ? minYVal.hashCode() : 0);
        result = 31 * result + (maxYVal != null ? maxYVal.hashCode() : 0);
        result = 31 * result + (avgYVal != null ? avgYVal.hashCode() : 0);
        result = 31 * result + (sumYVal != null ? sumYVal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Point{");
        sb.append("xVal=").append(xVal);
        sb.append(", minYVal=").append(minYVal);
        sb.append(", maxYVal=").append(maxYVal);
        sb.append(", avgYVal=").append(avgYVal);
        sb.append(", sumYVal=").append(sumYVal);
        sb.append('}');
        return sb.toString();
    }
}
