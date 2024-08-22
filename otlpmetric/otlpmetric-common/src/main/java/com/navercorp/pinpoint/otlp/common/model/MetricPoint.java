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

import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class MetricPoint <Y extends Number> {
    private final long xVal;
    private final Y yVal;

    public MetricPoint(long xVal, Y yVal) {
        this.xVal = xVal;
        this.yVal = yVal;
    }

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
        MetricPoint<?> that = (MetricPoint<?>) o;
        return xVal == that.xVal && Objects.equals(yVal, that.yVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xVal, yVal);
    }

    @Override
    public String toString() {
        return "MetricPoint{" +
                "xVal=" + xVal +
                ", yVal=" + yVal +
                '}';
    }
}

