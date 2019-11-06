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

package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class SampledDeadlock implements SampledAgentStatDataPoint {

    public static final int UNCOLLECTED_COUNT = -1;
    public static final Point.UncollectedPointCreator<AgentStatPoint<Integer>> UNCOLLECTED_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Integer>>() {
        @Override
        public AgentStatPoint<Integer> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, UNCOLLECTED_COUNT);
        }
    };

    private final AgentStatPoint<Integer> deadlockedThreadCount;

    public SampledDeadlock(AgentStatPoint<Integer> deadlockedThreadCount) {
        this.deadlockedThreadCount = Objects.requireNonNull(deadlockedThreadCount, "deadlockedThreadCount");
    }

    public AgentStatPoint<Integer> getDeadlockedThreadCount() {
        return deadlockedThreadCount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledDeadlock that = (SampledDeadlock) o;

        return deadlockedThreadCount != null ? deadlockedThreadCount.equals(that.deadlockedThreadCount) : that.deadlockedThreadCount == null;
    }

    @Override
    public int hashCode() {
        return deadlockedThreadCount != null ? deadlockedThreadCount.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledDeadlock{");
        sb.append("deadlockedThreadCount=").append(deadlockedThreadCount);
        sb.append('}');
        return sb.toString();
    }
}
