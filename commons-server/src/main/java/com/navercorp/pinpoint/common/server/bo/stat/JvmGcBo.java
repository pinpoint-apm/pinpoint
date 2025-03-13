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

package com.navercorp.pinpoint.common.server.bo.stat;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;

/**
 * @author HyunGil Jeong
 */
public class JvmGcBo extends AgentStatDataBasePoint {

    public static final long UNCOLLECTED_VALUE = -1;

    private JvmGcType gcType = JvmGcType.UNKNOWN;
    private long heapUsed = UNCOLLECTED_VALUE;
    private long heapMax = UNCOLLECTED_VALUE;
    private long nonHeapUsed = UNCOLLECTED_VALUE;
    private long nonHeapMax = UNCOLLECTED_VALUE;
    private long gcOldCount = UNCOLLECTED_VALUE;
    private long gcOldTime = UNCOLLECTED_VALUE;


    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.JVM_GC;
    }

    public JvmGcType getGcType() {
        return gcType;
    }

    public void setGcType(JvmGcType gcType) {
        this.gcType = gcType;
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;
    }

    public long getHeapMax() {
        return heapMax;
    }

    public void setHeapMax(long heapMax) {
        this.heapMax = heapMax;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed;
    }

    public void setNonHeapUsed(long nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }

    public long getNonHeapMax() {
        return nonHeapMax;
    }

    public void setNonHeapMax(long nonHeapMax) {
        this.nonHeapMax = nonHeapMax;
    }

    public long getGcOldCount() {
        return gcOldCount;
    }

    public void setGcOldCount(long gcOldCount) {
        this.gcOldCount = gcOldCount;
    }

    public long getGcOldTime() {
        return gcOldTime;
    }

    public void setGcOldTime(long gcOldTime) {
        this.gcOldTime = gcOldTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JvmGcBo jvmGcBo = (JvmGcBo) o;

        if (startTimestamp != jvmGcBo.startTimestamp) return false;
        if (timestamp != jvmGcBo.timestamp) return false;
        if (heapUsed != jvmGcBo.heapUsed) return false;
        if (heapMax != jvmGcBo.heapMax) return false;
        if (nonHeapUsed != jvmGcBo.nonHeapUsed) return false;
        if (nonHeapMax != jvmGcBo.nonHeapMax) return false;
        if (gcOldCount != jvmGcBo.gcOldCount) return false;
        if (gcOldTime != jvmGcBo.gcOldTime) return false;
        if (agentId != null ? !agentId.equals(jvmGcBo.agentId) : jvmGcBo.agentId != null) return false;
        return gcType == jvmGcBo.gcType;

    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + Long.hashCode(startTimestamp);
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + (gcType != null ? gcType.hashCode() : 0);
        result = 31 * result + Long.hashCode(heapUsed);
        result = 31 * result + Long.hashCode(heapMax);
        result = 31 * result + Long.hashCode(nonHeapUsed);
        result = 31 * result + Long.hashCode(nonHeapMax);
        result = 31 * result + Long.hashCode(gcOldCount);
        result = 31 * result + Long.hashCode(gcOldTime);
        return result;
    }

    @Override
    public String toString() {
        return "JvmGcBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                ", gcType=" + gcType +
                ", heapUsed=" + heapUsed +
                ", heapMax=" + heapMax +
                ", nonHeapUsed=" + nonHeapUsed +
                ", nonHeapMax=" + nonHeapMax +
                ", gcOldCount=" + gcOldCount +
                ", gcOldTime=" + gcOldTime +
                '}';
    }
}
