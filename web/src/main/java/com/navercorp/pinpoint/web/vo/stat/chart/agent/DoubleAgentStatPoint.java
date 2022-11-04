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
import com.navercorp.pinpoint.web.view.agentstat.DoubleAgentStatPointSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.function.DoubleConsumer;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = DoubleAgentStatPointSerializer.class)
public class DoubleAgentStatPoint implements Point, DoubleConsumer {

    private final long timestamp;
    private double min;
    private double max;
    private long count;
    private double sum;
    private double sumCompensation; // Low order bits of sum
    private double simpleSum; // Used to compute right sum for non-finite inputs

    public DoubleAgentStatPoint(long timestamp) {
        this.timestamp = timestamp;
        this.min = Double.POSITIVE_INFINITY;
        this.max = Double.NEGATIVE_INFINITY;
    }

    public DoubleAgentStatPoint(long timestamp, double min, double max, long count, double sum) {
        this.timestamp = timestamp;
        this.min = min;
        this.max = max;
        this.count = count;
        this.sum = sum;
    }

    public static DoubleAgentStatPoint ofSingle(long timestamp, double single) {
        return new DoubleAgentStatPoint(timestamp, single, single, 1, single);
    }

    public static DoubleAgentStatPoint ofUnCollected(long timestamp, double uncollected) {
        return new DoubleAgentStatPoint(timestamp, uncollected, uncollected, 0, uncollected);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public long getCount() {
        return count;
    }

    public double getAvg() {
        return getCount() > 0 ? getSum() / getCount() : 0.0d;
    }

    public double getSum() {
        double tmp =  sum + sumCompensation;
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum)) {
            return simpleSum;
        }
        else {
            return tmp;
        }
    }

    private void sumWithCompensation(double value) {
        double tmp = value - sumCompensation;
        double velvel = sum + tmp; // Little wolf of rounding error
        sumCompensation = (velvel - sum) - tmp;
        sum = velvel;
    }

    @Override
    public void accept(double value) {
        ++count;
        simpleSum += value;
        sumWithCompensation(value);
        min = Math.min(min, value);
        max = Math.max(max, value);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoubleAgentStatPoint that = (DoubleAgentStatPoint) o;

        if (timestamp != that.timestamp) return false;
        if (Double.compare(that.min, min) != 0) return false;
        if (Double.compare(that.max, max) != 0) return false;
        if (count != that.count) return false;
        if (Double.compare(that.sum, sum) != 0) return false;
        if (Double.compare(that.sumCompensation, sumCompensation) != 0) return false;
        return Double.compare(that.simpleSum, simpleSum) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (timestamp ^ (timestamp >>> 32));
        temp = Double.doubleToLongBits(min);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(max);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (count ^ (count >>> 32));
        temp = Double.doubleToLongBits(sum);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sumCompensation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(simpleSum);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DoubleAgentStatPoint{" +
                "xVal=" + timestamp +
                ", min=" + min +
                ", max=" + max +
                ", count=" + count +
                ", sum=" + sum +
                ", sumCompensation=" + sumCompensation +
                ", simpleSum=" + simpleSum +
                ", avg=" + getAvg() +
                '}';
    }

}
