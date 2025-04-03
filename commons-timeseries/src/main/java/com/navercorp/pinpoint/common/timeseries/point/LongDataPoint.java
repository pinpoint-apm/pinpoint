/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.timeseries.point;

public class LongDataPoint implements DataPoint<Long> {

    private final long timestamp;
    private final long value;

    public LongDataPoint(long timestamp, long value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Long getValue() {
        return value;
    }

    public long getLongValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        LongDataPoint that = (LongDataPoint) o;
        return timestamp == that.timestamp && value == that.value;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(timestamp);
        result = 31 * result + Long.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return "LongDataPoint{" +
                 timestamp +
                " -> " +
                value +
                '}';
    }
}
