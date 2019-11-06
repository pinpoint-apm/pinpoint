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

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class JvmGc {

    private final String agentId;
    private final long timestamp;

    private long heapUsed;
    private long heapMax;
    private long nonHeapUsed;
    private long nonHeapMax;
    private long gcOldCount;
    private long gcOldTime;

    public JvmGc(String agentId, long timestamp) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.timestamp = timestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getTimestamp() {
        return timestamp;
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

        JvmGc jvmGc = (JvmGc) o;

        if (timestamp != jvmGc.timestamp) return false;
        return agentId.equals(jvmGc.agentId);

    }

    @Override
    public int hashCode() {
        int result = agentId.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JvmGc{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", timestamp=").append(timestamp);
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
