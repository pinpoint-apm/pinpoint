/*
 * Copyright 2020 NAVER Corp.
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

public class LoadedClassBo implements AgentStatDataPoint {
    public static final long UNCOLLECTED_VALUE = -1L;

    private String agentId;
    private long startTimestamp;
    private long timestamp;

    private long loadedClassCount = UNCOLLECTED_VALUE;
    private long unloadedClassCount = UNCOLLECTED_VALUE;

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
        return AgentStatType.LOADED_CLASS;
    }

    public long getLoadedClassCount() {
        return loadedClassCount;
    }

    public void setLoadedClassCount(long loadedClassCount) {
        this.loadedClassCount = loadedClassCount;
    }

    public long getUnloadedClassCount() {
        return unloadedClassCount;
    }

    public void setUnloadedClassCount(long unloadedClassCount) {
        this.unloadedClassCount = unloadedClassCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoadedClassBo loadedClassBo = (LoadedClassBo) o;

        if (startTimestamp != loadedClassBo.startTimestamp) return false;
        if (timestamp != loadedClassBo.timestamp) return false;
        if (loadedClassCount != loadedClassBo.loadedClassCount) return false;
        if (unloadedClassCount != loadedClassBo.unloadedClassCount) return false;
        return agentId != null ? agentId.equals(loadedClassBo.agentId) : loadedClassBo.agentId == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (loadedClassCount ^ (loadedClassCount >>> 32));
        result = 31 * result + (int) (unloadedClassCount ^ (unloadedClassCount >>> 32));
        return result;
    }
    @Override
    public String toString() {
        return "LoadedClassBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                ", loadedClassCount=" + loadedClassCount +
                ", unloadedClassCount=" + unloadedClassCount +
                '}';
    }
}
