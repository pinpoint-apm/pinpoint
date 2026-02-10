/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

import java.util.Objects;

/**
 * 
 * @author netspider
 * @author HyunGil Jeong
 */
public class AgentStatus {

    public static final AgentStatus UNKNOWN = new AgentStatus("unknown", AgentLifeCycleState.UNKNOWN, 0);

    private final String agentId;

    private final long eventTimestamp;

    private final AgentLifeCycleState state;

    public AgentStatus(String agentId, AgentLifeCycleState state, long eventTimestamp) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.state = Objects.requireNonNull(state, "state");
        this.eventTimestamp = eventTimestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public AgentLifeCycleState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentStatus that = (AgentStatus) o;
        return eventTimestamp == that.eventTimestamp && agentId.equals(that.agentId) && state == that.state;
    }

    @Override
    public int hashCode() {
        int result = agentId.hashCode();
        result = 31 * result + Long.hashCode(eventTimestamp);
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AgentStatus{" +
                "agentId='" + agentId + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                ", state=" + state +
                '}';
    }
}
