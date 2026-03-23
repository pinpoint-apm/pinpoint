/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.dao.model;

/**
 * Query result type for batched multi-agent queries.
 * Holds agentId alongside the time/value pair so results from all agents
 * can be returned in a single query and grouped by agentId afterward.
 */
public class AgentStatPoint {

    private final String agentId;
    private final long avgTime;
    private final double avgValue;

    public AgentStatPoint(String agentId, long avgTime, double avgValue) {
        this.agentId = agentId;
        this.avgTime = avgTime;
        this.avgValue = avgValue;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAvgTime() {
        return avgTime;
    }

    public double getAvgValue() {
        return avgValue;
    }
}
