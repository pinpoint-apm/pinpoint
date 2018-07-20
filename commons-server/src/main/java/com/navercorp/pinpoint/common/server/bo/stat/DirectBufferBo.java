/*
 * Copyright 2018 Naver Corp.
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

/**
 * @author Roy Kim
 */
public class DirectBufferBo implements AgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;

    private long directCount = UNCOLLECTED_VALUE;;
    private long directMemoryUsed = UNCOLLECTED_VALUE;;
    private long mappedCount = UNCOLLECTED_VALUE;;
    private long mappedMemoryUsed = UNCOLLECTED_VALUE;;

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
        return AgentStatType.DIRECT_BUFFER;
    }

    public long getDirectCount() {
        return directCount;
    }

    public void setDirectCount(long directCount) {
        this.directCount = directCount;
    }

    public long getDirectMemoryUsed() {
        return directMemoryUsed;
    }

    public void setDirectMemoryUsed(long directMemoryUsed) {
        this.directMemoryUsed = directMemoryUsed;
    }

    public long getMappedCount() {
        return mappedCount;
    }

    public void setMappedCount(long mappedCount) {
        this.mappedCount = mappedCount;
    }

    public long getMappedMemoryUsed() {
        return mappedMemoryUsed;
    }

    public void setMappedMemoryUsed(long mappedMemoryUsed) {
        this.mappedMemoryUsed = mappedMemoryUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirectBufferBo directBufferBo = (DirectBufferBo) o;

        if (startTimestamp != directBufferBo.startTimestamp) return false;
        if (timestamp != directBufferBo.timestamp) return false;
        if (directCount != directBufferBo.directCount) return false;
        if (directMemoryUsed != directBufferBo.directMemoryUsed) return false;
        if (mappedCount != directBufferBo.mappedCount) return false;
        if (mappedMemoryUsed != directBufferBo.mappedMemoryUsed) return false;
        return agentId != null ? agentId.equals(directBufferBo.agentId) : directBufferBo.agentId == null;

    }

    @Override
    public int hashCode() {
        int result;
        result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (directCount ^ (directCount >>> 32));
        result = 31 * result + (int) (directMemoryUsed ^ (directMemoryUsed >>> 32));
        result = 31 * result + (int) (mappedCount ^ (mappedCount >>> 32));
        result = 31 * result + (int) (mappedMemoryUsed ^ (mappedMemoryUsed >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DirectBufferBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                ", directCount=" + directCount +
                ", directMemoryUsed=" + directMemoryUsed +
                ", mappedCount=" + mappedCount +
                ", mappedMemoryUsed=" + mappedMemoryUsed +
                '}';
    }
}
