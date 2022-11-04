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
import com.navercorp.pinpoint.web.view.agentstat.IntAgentStatPointSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.function.IntConsumer;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = IntAgentStatPointSerializer.class)
public class IntAgentStatPoint implements Point, IntConsumer {

    private final long timestamp;
    private int min;
    private int max;

    private long count;
    private long sum;

    public IntAgentStatPoint(long timestamp) {
        this.timestamp = timestamp;
        this.min = Integer.MAX_VALUE;
        this.max = Integer.MIN_VALUE;
    }

    public IntAgentStatPoint(long timestamp, int min, int max, long count, long sum) {
        this.timestamp = timestamp;
        this.min = min;
        this.max = max;
        this.count = count;
        this.sum = sum;
    }

    public static IntAgentStatPoint ofSingle(long timestamp, int single) {
        return new IntAgentStatPoint(timestamp, single, single, 1, single);
    }

    public static IntAgentStatPoint ofUnCollected(long timestamp, int uncollected) {
        return new IntAgentStatPoint(timestamp, uncollected, uncollected, 0, uncollected);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
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
    public void accept(int value) {
        ++count;
        sum += value;
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntAgentStatPoint that = (IntAgentStatPoint) o;

        if (timestamp != that.timestamp) return false;
        if (min != that.min) return false;
        if (max != that.max) return false;
        if (count != that.count) return false;
        return sum == that.sum;
    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + min;
        result = 31 * result + max;
        result = 31 * result + (int) (count ^ (count >>> 32));
        result = 31 * result + (int) (sum ^ (sum >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "IntAgentStatPoint{" +
                "xVal=" + timestamp +
                ", min=" + min +
                ", max=" + max +
                ", count=" + count +
                ", sum=" + sum +
                ", avg=" + getAvg() +
                '}';
    }

}
