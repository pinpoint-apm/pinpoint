/*
 * Copyright 2020 Naver Corp.
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

public class TotalThreadCountBo implements AgentStatDataPoint {
    public static final long UNCOLLECTED_VALUE = -1L;

    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private long totalThreadCount = UNCOLLECTED_VALUE;

    @Override
    public String getAgentId() { return agentId; }

    @Override
    public void setAgentId(String agentId) { this.agentId = agentId; }

    @Override
    public long getStartTimestamp() { return startTimestamp; }

    @Override
    public void setStartTimestamp(long startTimestamp) { this.startTimestamp = startTimestamp; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public AgentStatType getAgentStatType() { return AgentStatType.TOTAL_THREAD; }

    public long getTotalThreadCount() { return totalThreadCount; }

    public void setTotalThreadCount(long totalThreadCount) { this.totalThreadCount = totalThreadCount; }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TotalThreadCountBo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", totalThreadCount=").append(totalThreadCount);
        sb.append('}');
        return sb.toString();
    }
}
