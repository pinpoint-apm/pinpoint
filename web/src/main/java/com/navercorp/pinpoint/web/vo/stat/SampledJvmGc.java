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

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPointCreatorFactory;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

/**
 * @author HyunGil Jeong
 */
public class SampledJvmGc implements SampledAgentStatDataPoint {

    public static final Long UNCOLLECTED_VALUE = -1L;
    public static final Point.UncollectedPointCreator<AgentStatPoint<Long>> UNCOLLECTED_POINT_CREATOR = UncollectedPointCreatorFactory.createLongPointCreator(UNCOLLECTED_VALUE);

    private JvmGcType jvmGcType;
    private AgentStatPoint<Long> heapUsed;
    private AgentStatPoint<Long> heapMax;
    private AgentStatPoint<Long> nonHeapUsed;
    private AgentStatPoint<Long> nonHeapMax;
    private AgentStatPoint<Long> gcOldCount;
    private AgentStatPoint<Long> gcOldTime;

    public JvmGcType getJvmGcType() {
        return jvmGcType;
    }

    public void setJvmGcType(JvmGcType jvmGcType) {
        this.jvmGcType = jvmGcType;
    }

    public AgentStatPoint<Long> getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(AgentStatPoint<Long> heapUsed) {
        this.heapUsed = heapUsed;
    }

    public AgentStatPoint<Long> getHeapMax() {
        return heapMax;
    }

    public void setHeapMax(AgentStatPoint<Long> heapMax) {
        this.heapMax = heapMax;
    }

    public AgentStatPoint<Long> getNonHeapUsed() {
        return nonHeapUsed;
    }

    public void setNonHeapUsed(AgentStatPoint<Long> nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }

    public AgentStatPoint<Long> getNonHeapMax() {
        return nonHeapMax;
    }

    public void setNonHeapMax(AgentStatPoint<Long> nonHeapMax) {
        this.nonHeapMax = nonHeapMax;
    }

    public AgentStatPoint<Long> getGcOldCount() {
        return gcOldCount;
    }

    public void setGcOldCount(AgentStatPoint<Long> gcOldCount) {
        this.gcOldCount = gcOldCount;
    }

    public AgentStatPoint<Long> getGcOldTime() {
        return gcOldTime;
    }

    public void setGcOldTime(AgentStatPoint<Long> gcOldTime) {
        this.gcOldTime = gcOldTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledJvmGc that = (SampledJvmGc) o;

        if (jvmGcType != that.jvmGcType) return false;
        if (heapUsed != null ? !heapUsed.equals(that.heapUsed) : that.heapUsed != null) return false;
        if (heapMax != null ? !heapMax.equals(that.heapMax) : that.heapMax != null) return false;
        if (nonHeapUsed != null ? !nonHeapUsed.equals(that.nonHeapUsed) : that.nonHeapUsed != null) return false;
        if (nonHeapMax != null ? !nonHeapMax.equals(that.nonHeapMax) : that.nonHeapMax != null) return false;
        if (gcOldCount != null ? !gcOldCount.equals(that.gcOldCount) : that.gcOldCount != null) return false;
        return gcOldTime != null ? gcOldTime.equals(that.gcOldTime) : that.gcOldTime == null;
    }

    @Override
    public int hashCode() {
        int result = jvmGcType != null ? jvmGcType.hashCode() : 0;
        result = 31 * result + (heapUsed != null ? heapUsed.hashCode() : 0);
        result = 31 * result + (heapMax != null ? heapMax.hashCode() : 0);
        result = 31 * result + (nonHeapUsed != null ? nonHeapUsed.hashCode() : 0);
        result = 31 * result + (nonHeapMax != null ? nonHeapMax.hashCode() : 0);
        result = 31 * result + (gcOldCount != null ? gcOldCount.hashCode() : 0);
        result = 31 * result + (gcOldTime != null ? gcOldTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledJvmGc{");
        sb.append("jvmGcType=").append(jvmGcType);
        sb.append(", heapUsed=").append(heapUsed);
        sb.append(", heapMax=").append(heapMax);
        sb.append(", nonHeapUsed=").append(nonHeapUsed);
        sb.append(", nonHeapMax=").append(nonHeapMax);
        sb.append(", gcOldCount=").append(gcOldCount);
        sb.append(", gcOldTime=").append(gcOldTime);
        sb.append('}');
        return sb.toString();
    }
}
