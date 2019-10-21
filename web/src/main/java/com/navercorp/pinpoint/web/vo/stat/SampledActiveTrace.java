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

package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class SampledActiveTrace implements SampledAgentStatDataPoint {

    public static final int UNCOLLECTED_COUNT = -1;
    public static final Point.UncollectedPointCreator<AgentStatPoint<Integer>> UNCOLLECTED_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Integer>>() {
        @Override
        public AgentStatPoint<Integer> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, UNCOLLECTED_COUNT);
        }
    };

    private final AgentStatPoint<Integer> fastCounts;
    private final AgentStatPoint<Integer> normalCounts;
    private final AgentStatPoint<Integer> slowCounts;
    private final AgentStatPoint<Integer> verySlowCounts;

    public SampledActiveTrace(AgentStatPoint<Integer> fastCounts, AgentStatPoint<Integer> normalCounts, AgentStatPoint<Integer> slowCounts, AgentStatPoint<Integer> verySlowCounts) {
        this.fastCounts = Objects.requireNonNull(fastCounts, "fastCounts");
        this.normalCounts = Objects.requireNonNull(normalCounts, "normalCounts");
        this.slowCounts = Objects.requireNonNull(slowCounts, "slowCounts");
        this.verySlowCounts = Objects.requireNonNull(verySlowCounts, "verySlowCounts");
    }

    public AgentStatPoint<Integer> getFastCounts() {
        return fastCounts;
    }

    public AgentStatPoint<Integer> getNormalCounts() {
        return normalCounts;
    }

    public AgentStatPoint<Integer> getSlowCounts() {
        return slowCounts;
    }


    public AgentStatPoint<Integer> getVerySlowCounts() {
        return verySlowCounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledActiveTrace that = (SampledActiveTrace) o;

        if (fastCounts != null ? !fastCounts.equals(that.fastCounts) : that.fastCounts != null) return false;
        if (normalCounts != null ? !normalCounts.equals(that.normalCounts) : that.normalCounts != null) return false;
        if (slowCounts != null ? !slowCounts.equals(that.slowCounts) : that.slowCounts != null) return false;
        return verySlowCounts != null ? verySlowCounts.equals(that.verySlowCounts) : that.verySlowCounts == null;
    }

    @Override
    public int hashCode() {
        int result = fastCounts != null ? fastCounts.hashCode() : 0;
        result = 31 * result + (normalCounts != null ? normalCounts.hashCode() : 0);
        result = 31 * result + (slowCounts != null ? slowCounts.hashCode() : 0);
        result = 31 * result + (verySlowCounts != null ? verySlowCounts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledActiveTrace{");
        sb.append("fastCounts=").append(fastCounts);
        sb.append(", normalCounts=").append(normalCounts);
        sb.append(", slowCounts=").append(slowCounts);
        sb.append(", verySlowCounts=").append(verySlowCounts);
        sb.append('}');
        return sb.toString();
    }
}
