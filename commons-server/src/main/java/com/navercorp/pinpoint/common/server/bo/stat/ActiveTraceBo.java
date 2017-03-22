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

import com.navercorp.pinpoint.common.trace.SlotType;

import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class ActiveTraceBo implements AgentStatDataPoint {

    public static final int UNCOLLECTED_ACTIVE_TRACE_COUNT = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private short version = 0;
    private int histogramSchemaType;
    private Map<SlotType, Integer> activeTraceCounts;

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
        return AgentStatType.ACTIVE_TRACE;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public int getHistogramSchemaType() {
        return histogramSchemaType;
    }

    public void setHistogramSchemaType(int histogramSchemaType) {
        this.histogramSchemaType = histogramSchemaType;
    }

    public Map<SlotType, Integer> getActiveTraceCounts() {
        return activeTraceCounts;
    }

    public void setActiveTraceCounts(Map<SlotType, Integer> activeTraceCounts) {
        this.activeTraceCounts = activeTraceCounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActiveTraceBo that = (ActiveTraceBo) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (version != that.version) return false;
        if (histogramSchemaType != that.histogramSchemaType) return false;
        if (agentId != null ? !agentId.equals(that.agentId) : that.agentId != null) return false;
        return activeTraceCounts != null ? activeTraceCounts.equals(that.activeTraceCounts) : that.activeTraceCounts == null;

    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) version;
        result = 31 * result + histogramSchemaType;
        result = 31 * result + (activeTraceCounts != null ? activeTraceCounts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ActiveTraceBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                ", version=" + version +
                ", histogramSchemaType=" + histogramSchemaType +
                ", activeTraceCounts=" + activeTraceCounts +
                '}';
    }
}
