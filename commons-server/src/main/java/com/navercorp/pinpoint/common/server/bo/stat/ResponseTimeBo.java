/*
 * Copyright 2017 NAVER Corp.
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
 * @author Taejin Koo
 */
public class ResponseTimeBo implements AgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private long avg = 0;

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
        return AgentStatType.RESPONSE_TIME;
    }

    public long getAvg() {
        return avg;
    }

    public void setAvg(long avg) {
        this.avg = avg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResponseTimeBo that = (ResponseTimeBo) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (avg != that.avg) return false;
        return agentId != null ? agentId.equals(that.agentId) : that.agentId == null;

    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (avg ^ (avg >>> 32));
        return result;
    }

}
