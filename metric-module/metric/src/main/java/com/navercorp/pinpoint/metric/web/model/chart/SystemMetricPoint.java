/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.model.chart;

import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricPoint<Y extends Number> implements Point {

    private final long xVal;
    private final Y yVal;

    public SystemMetricPoint(long xVal, Y yVal) {
        this.xVal = xVal;
        this.yVal = yVal;
    }

    @Override
    public long getXVal() {
        return xVal;
    }

    public Y getYVal() {
        return yVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SystemMetricPoint<?> that = (SystemMetricPoint<?>) o;

        if (xVal != that.xVal) return false;
        return Objects.equals(yVal, that.yVal);
    }

    @Override
    public int hashCode() {
        int result = (int) (xVal ^ (xVal >>> 32));
        result = 31 * result + (yVal != null ? yVal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemMetricPoint{");
        sb.append("xVal=").append(xVal);
        sb.append(", yVal=").append(yVal);
        sb.append('}');
        return sb.toString();
    }
}
