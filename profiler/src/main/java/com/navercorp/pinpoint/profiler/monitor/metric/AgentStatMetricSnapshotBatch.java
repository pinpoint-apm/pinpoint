/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class AgentStatMetricSnapshotBatch {
    private java.lang.String agentId;
    private long startTimestamp;
    private List<AgentStatMetricSnapshot> agentStats;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public List<AgentStatMetricSnapshot> getAgentStats() {
        return agentStats;
    }

    public void setAgentStats(List<AgentStatMetricSnapshot> agentStats) {
        this.agentStats = agentStats;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentStatMetricSnapshotBatch{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", agentStats=").append(agentStats);
        sb.append('}');
        return sb.toString();
    }
}