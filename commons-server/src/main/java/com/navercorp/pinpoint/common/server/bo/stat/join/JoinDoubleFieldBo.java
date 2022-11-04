/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.stat.join;

import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Taejin Koo
 */
public class JoinDoubleFieldBo implements JoinFieldBo, Consumer<JoinDoubleFieldBo> {
    private double min;
    private String minAgentId;
    private double max;
    private String maxAgentId;

    private long count;
    private double sum;
    private double sumCompensation;
    private double simpleSum;

    private static final double UNCOLLECTED_VALUE = -1;
    static final JoinDoubleFieldBo UNCOLLECTED_FIELD_BO = new JoinDoubleFieldBo(UNCOLLECTED_VALUE, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT);

    public JoinDoubleFieldBo() {
        this.min = Double.POSITIVE_INFINITY;
        this.minAgentId = JoinStatBo.UNKNOWN_AGENT;
        this.max = Double.NEGATIVE_INFINITY;
        this.maxAgentId = JoinStatBo.UNKNOWN_AGENT;
    }

    public JoinDoubleFieldBo(double avg, double min, String minAgentId, double max, String maxAgentId) {
        this.sum = avg;
        this.count = 1;
        this.min = min;
        this.minAgentId = Objects.requireNonNull(minAgentId, "minAgentId");
        this.max = max;
        this.maxAgentId = Objects.requireNonNull(maxAgentId, "maxAgentId");
    }

    public double getAvg() {
        return getCount() > 0 ? getSum() / getCount() : 0.0d;
    }

    public long getCount() {
        return count;
    }

    public double getSum() {
        double tmp =  sum + sumCompensation;
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum)) {
            return simpleSum;
        } else {
            return tmp;
        }
    }

    public double getMin() {
        return min;
    }

    public String getMinAgentId() {
        return minAgentId;
    }

    public double getMax() {
        return max;
    }

    public String getMaxAgentId() {
        return maxAgentId;
    }

    protected static JoinDoubleFieldBo merge(List<JoinDoubleFieldBo> joinDoubleFieldBoList) {
        if (CollectionUtils.isEmpty(joinDoubleFieldBoList)) {
            return UNCOLLECTED_FIELD_BO;
        }
        JoinDoubleFieldBo merge = new JoinDoubleFieldBo();
        joinDoubleFieldBoList.forEach(merge);
        return merge;
    }

    @Override
    public void accept(JoinDoubleFieldBo other) {
        count += other.count;
        simpleSum += other.simpleSum;
        sumWithCompensation(other.sum);
        sumWithCompensation(other.sumCompensation);
        if (Double.compare(min, other.min) == 1) {
            this.min = other.min;
            this.minAgentId = other.minAgentId;
        }
        if (Double.compare(max, other.max) == -1) {
            this.max = other.max;
            this.maxAgentId = other.maxAgentId;
        }
    }

    private void sumWithCompensation(double value) {
        double tmp = value - sumCompensation;
        double velvel = sum + tmp;
        sumCompensation = (velvel - sum) - tmp;
        sum = velvel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinDoubleFieldBo that = (JoinDoubleFieldBo) o;

        if (Double.compare(that.min, min) != 0) return false;
        if (Double.compare(that.max, max) != 0) return false;
        if (Double.compare(that.getAvg(), getAvg()) != 0) return false;
        if (minAgentId != null ? !minAgentId.equals(that.minAgentId) : that.minAgentId != null) return false;
        return maxAgentId != null ? maxAgentId.equals(that.maxAgentId) : that.maxAgentId == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(min);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (minAgentId != null ? minAgentId.hashCode() : 0);
        temp = Double.doubleToLongBits(max);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (maxAgentId != null ? maxAgentId.hashCode() : 0);
        temp = Double.doubleToLongBits(getAvg());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JoinDoubleFieldBo{" +
                "avg=" + getAvg() +
                ", min=" + min +
                ", minAgentId='" + minAgentId + '\'' +
                ", max=" + max +
                ", maxAgentId='" + maxAgentId + '\'' +
                '}';
    }
}

