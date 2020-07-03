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

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Taejin Koo
 */
public abstract class AbstractJoinFieldBo<V extends Number> implements JoinFieldBo<V> {

    private final V avgValue;

    private final V minValue;

    private final String minAgentId;

    private final V maxValue;

    private final String maxAgentid;

    public AbstractJoinFieldBo(V avgValue, V minValue, String minAgentId, V maxValue, String maxAgentid) {
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.minAgentId = Objects.requireNonNull(minAgentId, "minAgentId");
        this.maxValue = maxValue;
        this.maxAgentid = Objects.requireNonNull(maxAgentid, "maxAgentid");
    }

    @Override
    public V getAvg() {
        return avgValue;
    }

    @Override
    public V getMin() {
        return minValue;
    }

    @Override
    public String getMinAgentId() {
        return minAgentId;
    }

    @Override
    public V getMax() {
        return maxValue;
    }

    @Override
    public String getMaxAgentId() {
        return maxAgentid;
    }

    abstract protected JoinFieldBo<V> getUncollectedValue();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractJoinFieldBo<?> that = (AbstractJoinFieldBo<?>) o;

        if (avgValue != null ? !avgValue.equals(that.avgValue) : that.avgValue != null) return false;
        if (minValue != null ? !minValue.equals(that.minValue) : that.minValue != null) return false;
        if (minAgentId != null ? !minAgentId.equals(that.minAgentId) : that.minAgentId != null) return false;
        if (maxValue != null ? !maxValue.equals(that.maxValue) : that.maxValue != null) return false;
        return maxAgentid != null ? maxAgentid.equals(that.maxAgentid) : that.maxAgentid == null;
    }

    @Override
    public int hashCode() {
        int result = avgValue != null ? avgValue.hashCode() : 0;
        result = 31 * result + (minValue != null ? minValue.hashCode() : 0);
        result = 31 * result + (minAgentId != null ? minAgentId.hashCode() : 0);
        result = 31 * result + (maxValue != null ? maxValue.hashCode() : 0);
        result = 31 * result + (maxAgentid != null ? maxAgentid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("avgValue=" + avgValue)
                .add("minValue=" + minValue)
                .add("minAgentId='" + minAgentId + "'")
                .add("maxValue=" + maxValue)
                .add("maxAgentid='" + maxAgentid + "'")
                .toString();
    }

}

