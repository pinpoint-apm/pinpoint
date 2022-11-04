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
import com.navercorp.pinpoint.web.vo.chart.UncollectedPointCreatorFactory;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DoubleAgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.LongAgentStatPoint;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class SampledJvmGcDetailed implements SampledAgentStatDataPoint {

    public static final Long UNCOLLECTED_VALUE = -1L;
    public static final Point.UncollectedPointCreator<LongAgentStatPoint> UNCOLLECTED_VALUE_POINT_CREATOR = UncollectedPointCreatorFactory.createLongPointCreator(UNCOLLECTED_VALUE);

    public static final Double UNCOLLECTED_PERCENTAGE = -1D;
    public static final Point.UncollectedPointCreator<DoubleAgentStatPoint> UNCOLLECTED_PERCENTAGE_POINT_CREATOR = UncollectedPointCreatorFactory.createDoublePointCreator(UNCOLLECTED_VALUE);

    private final LongAgentStatPoint gcNewCount;
    private final LongAgentStatPoint gcNewTime;
    private final DoubleAgentStatPoint codeCacheUsed;
    private final DoubleAgentStatPoint newGenUsed;
    private final DoubleAgentStatPoint oldGenUsed;
    private final DoubleAgentStatPoint survivorSpaceUsed;
    private final DoubleAgentStatPoint permGenUsed;
    private final DoubleAgentStatPoint metaspaceUsed;

    public SampledJvmGcDetailed(LongAgentStatPoint gcNewCount, LongAgentStatPoint gcNewTime, DoubleAgentStatPoint codeCacheUsed, DoubleAgentStatPoint newGenUsed,
                                DoubleAgentStatPoint oldGenUsed, DoubleAgentStatPoint survivorSpaceUsed, DoubleAgentStatPoint permGenUsed, DoubleAgentStatPoint metaspaceUsed) {
        this.gcNewCount = Objects.requireNonNull(gcNewCount, "gcNewCount");
        this.gcNewTime = Objects.requireNonNull(gcNewTime, "gcNewTime");
        this.codeCacheUsed = Objects.requireNonNull(codeCacheUsed, "codeCacheUsed");
        this.newGenUsed = Objects.requireNonNull(newGenUsed, "newGenUsed");
        this.oldGenUsed = Objects.requireNonNull(oldGenUsed, "oldGenUsed");
        this.survivorSpaceUsed = Objects.requireNonNull(survivorSpaceUsed, "survivorSpaceUsed");
        this.permGenUsed = Objects.requireNonNull(permGenUsed, "permGenUsed");
        this.metaspaceUsed = Objects.requireNonNull(metaspaceUsed, "metaspaceUsed");
    }

    public LongAgentStatPoint getGcNewCount() {
        return gcNewCount;
    }

    public LongAgentStatPoint getGcNewTime() {
        return gcNewTime;
    }

    public DoubleAgentStatPoint getCodeCacheUsed() {
        return codeCacheUsed;
    }

    public DoubleAgentStatPoint getNewGenUsed() {
        return newGenUsed;
    }

    public DoubleAgentStatPoint getOldGenUsed() {
        return oldGenUsed;
    }

    public DoubleAgentStatPoint getSurvivorSpaceUsed() {
        return survivorSpaceUsed;
    }

    public DoubleAgentStatPoint getPermGenUsed() {
        return permGenUsed;
    }

    public DoubleAgentStatPoint getMetaspaceUsed() {
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
