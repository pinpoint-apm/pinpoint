/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.agentstat.LongAgentStatPointSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.function.LongConsumer;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = LongAgentStatPointSerializer.class)
public class LongAgentStatPoint implements Point, LongConsumer {

    private final long timestamp;
    private long min;
    private long max;
    private long count;
    private long sum;

    public LongAgentStatPoint(long timestamp) {
        this.timestamp = timestamp;
        this.min = Integer.MAX_VALUE;
        this.max = Integer.MIN_VALUE;
    }


    public LongAgentStatPoint(long timestamp, long min, long max, long count, long sum) {
        this.timestamp = timestamp;
        this.min = min;
        this.max = max;
        this.count = count;
        this.sum = sum;
    }

    public static LongAgentStatPoint ofUnCollected(long timestamp, long uncollected) {
        return new LongAgentStatPoint(timestamp, uncollected, uncollected, 0, uncollected);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public double getAvg() {
        return getCount() > 0 ? getSum() / getCount() : 0.0d;
    }

    public long getCount() {
        return count;
    }

    public long getSum() {
        return sum;
    }

    @Override
    public void accept(long value) {
        ++count;
        sum += value;
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongAgentStatPoint that = (LongAgentStatPoint) o;

        if (timestamp != that.timestamp) return false;
        if (min != that.min) return false;
        if (max != that.max) return false;
        if (count != that.count) return false;
        return sum == that.sum;
    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (min ^ (min >>> 32));
        result = 31 * result + (int) (max ^ (max >>> 32));
        result = 31 * result + (int) (count ^ (count >>> 32));
        result = 31 * result + (int) (sum ^ (sum >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LongAgentStatPoint{" +
                "xVal=" + timestamp +
                ", min=" + min +
                ", max=" + max +
                ", count=" + count +
                ", sum=" + sum +
                ", avg=" + getAvg() +
                '}';
    }
}
