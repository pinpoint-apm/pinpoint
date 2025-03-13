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


/**
 * @author HyunGil Jeong
 */
public class ActiveTraceBo extends AgentStatDataBasePoint {

    public static final int UNCOLLECTED_ACTIVE_TRACE_COUNT = -1;

    private short version = 0;
    private int histogramSchemaType;
    private ActiveTraceHistogram activeTraceHistogram;

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

    public ActiveTraceHistogram getActiveTraceHistogram() {
        return activeTraceHistogram;
    }

    public void setActiveTraceHistogram(ActiveTraceHistogram activeTraceHistogram) {
        this.activeTraceHistogram = activeTraceHistogram;
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
        return activeTraceHistogram != null ? activeTraceHistogram.equals(that.activeTraceHistogram) : that.activeTraceHistogram == null;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + Long.hashCode(startTimestamp);
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + (int) version;
        result = 31 * result + histogramSchemaType;
        result = 31 * result + (activeTraceHistogram != null ? activeTraceHistogram.hashCode() : 0);
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
                ", activeTraceHistogram=" + activeTraceHistogram +
                '}';
    }

}
