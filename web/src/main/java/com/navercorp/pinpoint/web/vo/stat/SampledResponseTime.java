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
public class SampledResponseTime implements SampledAgentStatDataPoint {

    public static final long UNCOLLECTED_RESPONSE_TIME = -1L;
    public static final Point.UncollectedPointCreator<AgentStatPoint<Long>> UNCOLLECTED_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Long>>() {
        @Override
        public AgentStatPoint<Long> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, UNCOLLECTED_RESPONSE_TIME);
        }
    };

    private final AgentStatPoint<Long> avg;

    public SampledResponseTime(AgentStatPoint<Long> avg) {
        this.avg = Objects.requireNonNull(avg, "avg must not be null");
    }

    public AgentStatPoint<Long> getAvg() {
        return avg;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledResponseTime that = (SampledResponseTime) o;

        return avg != null ? avg.equals(that.avg) : that.avg == null;
    }

    @Override
    public int hashCode() {
        return avg != null ? avg.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledResponseTime{");
        sb.append("avg=").append(avg);
        sb.append('}');
        return sb.toString();
    }
}
