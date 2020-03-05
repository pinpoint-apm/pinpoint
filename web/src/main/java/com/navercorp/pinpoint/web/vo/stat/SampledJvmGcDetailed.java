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
public class SampledJvmGcDetailed implements SampledAgentStatDataPoint {

    public static final Long UNCOLLECTED_VALUE = -1L;
    public static final Double UNCOLLECTED_PERCENTAGE = -1D;
    public static final Point.UncollectedPointCreator<AgentStatPoint<Long>> UNCOLLECTED_VALUE_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Long>>() {
        @Override
        public AgentStatPoint<Long> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, UNCOLLECTED_VALUE);
        }
    };
    public static final Point.UncollectedPointCreator<AgentStatPoint<Double>> UNCOLLECTED_PERCENTAGE_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Double>>() {
        @Override
        public AgentStatPoint<Double> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, UNCOLLECTED_PERCENTAGE);
        }
    };

    private final AgentStatPoint<Long> gcNewCount;
    private final AgentStatPoint<Long> gcNewTime;
    private final AgentStatPoint<Double> codeCacheUsed;
    private final AgentStatPoint<Double> newGenUsed;
    private final AgentStatPoint<Double> oldGenUsed;
    private final AgentStatPoint<Double> survivorSpaceUsed;
    private final AgentStatPoint<Double> permGenUsed;
    private final AgentStatPoint<Double> metaspaceUsed;

    public SampledJvmGcDetailed(AgentStatPoint<Long> gcNewCount, AgentStatPoint<Long> gcNewTime, AgentStatPoint<Double> codeCacheUsed, AgentStatPoint<Double> newGenUsed,
                                AgentStatPoint<Double> oldGenUsed, AgentStatPoint<Double> survivorSpaceUsed, AgentStatPoint<Double> permGenUsed, AgentStatPoint<Double> metaspaceUsed) {
        this.gcNewCount = Objects.requireNonNull(gcNewCount, "gcNewCount");
        this.gcNewTime = Objects.requireNonNull(gcNewTime, "gcNewTime");
        this.codeCacheUsed = Objects.requireNonNull(codeCacheUsed, "codeCacheUsed");
        this.newGenUsed = Objects.requireNonNull(newGenUsed, "newGenUsed");
        this.oldGenUsed = Objects.requireNonNull(oldGenUsed, "oldGenUsed");
        this.survivorSpaceUsed = Objects.requireNonNull(survivorSpaceUsed, "survivorSpaceUsed");
        this.permGenUsed = Objects.requireNonNull(permGenUsed, "permGenUsed");
        this.metaspaceUsed = Objects.requireNonNull(metaspaceUsed, "metaspaceUsed");
    }

    public AgentStatPoint<Long> getGcNewCount() {
        return gcNewCount;
    }

    public AgentStatPoint<Long> getGcNewTime() {
        return gcNewTime;
    }

    public AgentStatPoint<Double> getCodeCacheUsed() {
        return codeCacheUsed;
    }

    public AgentStatPoint<Double> getNewGenUsed() {
        return newGenUsed;
    }

    public AgentStatPoint<Double> getOldGenUsed() {
        return oldGenUsed;
    }

    public AgentStatPoint<Double> getSurvivorSpaceUsed() {
        return survivorSpaceUsed;
    }

    public AgentStatPoint<Double> getPermGenUsed() {
        return permGenUsed;
    }

    public AgentStatPoint<Double> getMetaspaceUsed() {
        return metaspaceUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledJvmGcDetailed that = (SampledJvmGcDetailed) o;

        if (gcNewCount != null ? !gcNewCount.equals(that.gcNewCount) : that.gcNewCount != null) return false;
        if (gcNewTime != null ? !gcNewTime.equals(that.gcNewTime) : that.gcNewTime != null) return false;
        if (codeCacheUsed != null ? !codeCacheUsed.equals(that.codeCacheUsed) : that.codeCacheUsed != null)
            return false;
        if (newGenUsed != null ? !newGenUsed.equals(that.newGenUsed) : that.newGenUsed != null) return false;
        if (oldGenUsed != null ? !oldGenUsed.equals(that.oldGenUsed) : that.oldGenUsed != null) return false;
        if (survivorSpaceUsed != null ? !survivorSpaceUsed.equals(that.survivorSpaceUsed) : that.survivorSpaceUsed != null)
            return false;
        if (permGenUsed != null ? !permGenUsed.equals(that.permGenUsed) : that.permGenUsed != null) return false;
        return metaspaceUsed != null ? metaspaceUsed.equals(that.metaspaceUsed) : that.metaspaceUsed == null;
    }

    @Override
    public int hashCode() {
        int result = gcNewCount != null ? gcNewCount.hashCode() : 0;
        result = 31 * result + (gcNewTime != null ? gcNewTime.hashCode() : 0);
        result = 31 * result + (codeCacheUsed != null ? codeCacheUsed.hashCode() : 0);
        result = 31 * result + (newGenUsed != null ? newGenUsed.hashCode() : 0);
        result = 31 * result + (oldGenUsed != null ? oldGenUsed.hashCode() : 0);
        result = 31 * result + (survivorSpaceUsed != null ? survivorSpaceUsed.hashCode() : 0);
        result = 31 * result + (permGenUsed != null ? permGenUsed.hashCode() : 0);
        result = 31 * result + (metaspaceUsed != null ? metaspaceUsed.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledJvmGcDetailed{");
        sb.append("gcNewCount=").append(gcNewCount);
        sb.append(", gcNewTime=").append(gcNewTime);
        sb.append(", codeCacheUsed=").append(codeCacheUsed);
        sb.append(", newGenUsed=").append(newGenUsed);
        sb.append(", oldGenUsed=").append(oldGenUsed);
        sb.append(", survivorSpaceUsed=").append(survivorSpaceUsed);
        sb.append(", permGenUsed=").append(permGenUsed);
        sb.append(", metaspaceUsed=").append(metaspaceUsed);
        sb.append('}');
        return sb.toString();
    }
}
