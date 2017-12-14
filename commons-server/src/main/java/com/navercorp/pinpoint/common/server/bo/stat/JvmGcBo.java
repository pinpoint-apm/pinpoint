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
public class JvmGcBo implements AgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private JvmGcType gcType = JvmGcType.UNKNOWN;
    private long heapUsed = UNCOLLECTED_VALUE;
    private long heapMax = UNCOLLECTED_VALUE;
    private long nonHeapUsed = UNCOLLECTED_VALUE;
    private long nonHeapMax = UNCOLLECTED_VALUE;
    private long gcOldCount = UNCOLLECTED_VALUE;
    private long gcOldTime = UNCOLLECTED_VALUE;

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

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
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (gcType != null ? gcType.hashCode() : 0);
        result = 31 * result + (int) (heapUsed ^ (heapUsed >>> 32));
        result = 31 * result + (int) (heapMax ^ (heapMax >>> 32));
        result = 31 * result + (int) (nonHeapUsed ^ (nonHeapUsed >>> 32));
        result = 31 * result + (int) (nonHeapMax ^ (nonHeapMax >>> 32));
        result = 31 * result + (int) (gcOldCount ^ (gcOldCount >>> 32));
        result = 31 * result + (int) (gcOldTime ^ (gcOldTime >>> 32));
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
