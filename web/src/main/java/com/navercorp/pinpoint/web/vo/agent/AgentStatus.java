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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.view.StringPinpointIdSerializer;

import java.util.Objects;

/**
 * 
 * @author netspider
 * @author HyunGil Jeong
 */
public class AgentStatus {

    @JsonSerialize(using = StringPinpointIdSerializer.class)
    private final AgentId agentId;

    private long eventTimestamp;

    private final AgentLifeCycleState state;

    public AgentStatus(AgentId agentId, AgentLifeCycleState state, long eventTimestamp) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.state = Objects.requireNonNull(state, "state");
        this.eventTimestamp = eventTimestamp;
    }

    public AgentStatus(String agentId, AgentLifeCycleState state, long eventTimestamp) {
        this(AgentId.of(agentId), state, eventTimestamp);
    }

    public AgentId getAgentId() {
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

        if (eventTimestamp != that.eventTimestamp) return false;
        if (!agentId.equals(that.agentId)) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = agentId.hashCode();
        result = 31 * result + (int) (eventTimestamp ^ (eventTimestamp >>> 32));
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AgentStatus{" + "agentId='" + agentId + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                ", state=" + state +
                '}';
    }
}
