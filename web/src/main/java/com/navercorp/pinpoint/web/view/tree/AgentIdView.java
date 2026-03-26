/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.view.tree;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;

public final class AgentIdView {
    private final Application application;
    private final String agentId;
    private final long agentStartTime;
    private final String agentName;

    // State within the query range
    private final AgentLifeCycleState state;
    // Current state of the agent
    private final AgentLifeCycleState currentState;
    private final long currentStateTimestamp;

    public static AgentIdView of(AgentIdEntry agentIdEntry) {
        return of(agentIdEntry, null);
    }

    public static AgentIdView of(AgentIdEntry agentIdEntry, Long toTimestamp) {
        return of(agentIdEntry.getApplication(), agentIdEntry.getAgentId(), agentIdEntry.getAgentStartTime(), agentIdEntry.getAgentName(),
                agentIdEntry.getCurrentState(), agentIdEntry.getCurrentStateTimestamp(), toTimestamp);
    }

    public static AgentIdView of(Application application, String agentId, long agentStartTime, String agentName,
                                 AgentLifeCycleState currentState, long currentStateTimestamp, Long toTimestamp) {
        final AgentLifeCycleState effectiveStatus = calculateEffectiveState(currentState, currentStateTimestamp, toTimestamp);
        final String name = StringUtils.hasText(agentName) ? agentName : agentId;
        return new AgentIdView(application, agentId, agentStartTime, name,
                effectiveStatus, currentState, currentStateTimestamp);
    }

    private static AgentLifeCycleState calculateEffectiveState(AgentLifeCycleState currentState, long currentStateTimestamp, Long toTimestamp) {
        if (toTimestamp != null && currentStateTimestamp > toTimestamp) {
            return AgentLifeCycleState.RUNNING;
        }
        return currentState;
    }

    public AgentIdView(Application application, String agentId, long agentStartTime, String agentName,
                       AgentLifeCycleState state, AgentLifeCycleState currentState, long currentStateTimestamp) {
        this.application = application;
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.agentName = StringUtils.hasText(agentName) ? agentName : agentId;
        this.state = state;
        this.currentState = currentState;
        this.currentStateTimestamp = currentStateTimestamp;
    }

    public String getApplicationName() {
        return application.getApplicationName();
    }

    public String getServiceTypeName() {
        return application.getServiceType().getName();
    }

    public int getServiceTypeCode() {
        return application.getServiceType().getCode();
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public String getAgentName() {
        return agentName;
    }

    public AgentLifeCycleState getState() {
        return state;
    }

    public AgentLifeCycleState getCurrentState() {
        return currentState;
    }

    public long getCurrentStateTimestamp() {
        return currentStateTimestamp;
    }

}
