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

package com.navercorp.pinpoint.web.vo.timeline.inspector;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentEventTypeCategory;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.vo.AgentEvent;

import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public enum AgentState {
    RUNNING,
    UNSTABLE_RUNNING,
    SHUTDOWN,
    UNKNOWN;

    private static final Set<AgentEventType> AGENT_EVENT_TYPE_SET = AgentEventType.getTypesByCategory(AgentEventTypeCategory.AGENT_LIFECYCLE);

    public static AgentState fromAgentLifeCycleState(AgentLifeCycleState state) {
        return switch (state) {
            case RUNNING -> RUNNING;
            case SHUTDOWN, UNEXPECTED_SHUTDOWN -> SHUTDOWN;
            default -> UNKNOWN;
        };
    }

    public static AgentState fromAgentEvent(AgentEvent agentEvent) {
        AgentEventType eventType = AgentEventType.getTypeByCode(agentEvent.getEventTypeCode());
        if (eventType != null && AGENT_EVENT_TYPE_SET.contains(eventType)) {
            return switch (eventType) {
                case AGENT_CONNECTED, AGENT_PING -> RUNNING;
                case AGENT_SHUTDOWN, AGENT_UNEXPECTED_SHUTDOWN -> SHUTDOWN;
                default -> UNKNOWN;
            };
        } else {
            throw new IllegalArgumentException("agentEvent must be a life cycle event");
        }
    }
}
