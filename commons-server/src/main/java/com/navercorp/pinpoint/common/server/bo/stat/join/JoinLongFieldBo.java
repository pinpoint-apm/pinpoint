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
public class JoinLongFieldBo implements JoinFieldBo, Consumer<JoinLongFieldBo> {

    private long min;
    private String minAgentId;
    private long max;
    private String maxAgentId;

    private long count;
    private long sum;

    private static final long UNCOLLECTED_VALUE = -1L;
    static final JoinLongFieldBo UNCOLLECTED_FIELD_BO = new JoinLongFieldBo(UNCOLLECTED_VALUE, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT);

    public JoinLongFieldBo() {
        this.min = Long.MAX_VALUE;
        this.minAgentId = JoinStatBo.UNKNOWN_AGENT;
        this.max = Long.MIN_VALUE;
        this.maxAgentId = JoinStatBo.UNKNOWN_AGENT;
    }

    public JoinLongFieldBo(long avg, long min, String minAgentId, long max, String maxAgentId) {
        this.sum = avg;
        this.count = 1;
        this.min = min;
        this.minAgentId = Objects.requireNonNull(minAgentId, "minAgentId");
        this.max = max;
        this.maxAgentId = Objects.requireNonNull(maxAgentId, "maxAgentId");
    }

    public long getAvg() {
        return getCount() > 0 ? getSum() / getCount() : 0L;
    }

    public long getCount() {
        return count;
    }

    public long getSum() {
        return sum;
    }

    public long getMin() {
        return min;
    }

    public String getMinAgentId() {
        return minAgentId;
    }

    public long getMax() {
        return max;
    }

    public String getMaxAgentId() {
        return maxAgentId;
    }


    protected static JoinLongFieldBo merge(List<JoinLongFieldBo> joinLongFieldBoList) {
        if (CollectionUtils.isEmpty(joinLongFieldBoList)) {
            return UNCOLLECTED_FIELD_BO;
        }
        JoinLongFieldBo merge = new JoinLongFieldBo();
        joinLongFieldBoList.forEach(merge);
        return merge;
    }

    @Override
    public void accept(JoinLongFieldBo other) {
        Objects.requireNonNull(other, "other");

        count += other.count;
        sum += other.sum;
        if (this.min > other.min) {
            this.min = other.min;
            this.minAgentId = other.minAgentId;
        }
        if (this.max < other.max) {
            this.max = other.max;
            this.maxAgentId = other.maxAgentId;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinLongFieldBo that = (JoinLongFieldBo) o;

        if (getAvg() != that.getAvg()) return false;
        if (min != that.min) return false;
        if (max != that.max) return false;
        if (!minAgentId.equals(that.minAgentId)) return false;
        return maxAgentId.equals(that.maxAgentId);
    }

    @Override
    public int hashCode() {
        long avg = getAvg();
        int result = (int) (avg ^ (avg >>> 32));
        result = 31 * result + (int) (min ^ (min >>> 32));
        result = 31 * result + minAgentId.hashCode();
        result = 31 * result + (int) (max ^ (max >>> 32));
        result = 31 * result + maxAgentId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "JoinLongFieldBo{" +
                "avg=" + getAvg() +
                ", min=" + min +
                ", minAgentId='" + minAgentId + '\'' +
                ", max=" + max +
                ", maxAgentId='" + maxAgentId + '\'' +
                '}';
    }
}

