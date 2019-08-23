/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.collector.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;

public enum ManagedAgentLifeCycle {
    RUNNING(0, AgentLifeCycleState.RUNNING, AgentEventType.AGENT_CONNECTED, SocketStateCode.RUN_SIMPLEX,
            SocketStateCode.RUN_DUPLEX),

    CLOSED_BY_CLIENT(Integer.MAX_VALUE, AgentLifeCycleState.SHUTDOWN, AgentEventType.AGENT_SHUTDOWN,
            SocketStateCode.CLOSED_BY_CLIENT),

    UNEXPECTED_CLOSE_BY_CLIENT(Integer.MAX_VALUE, AgentLifeCycleState.UNEXPECTED_SHUTDOWN, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN,
            SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT),

    CLOSED_BY_SERVER(Integer.MAX_VALUE, AgentLifeCycleState.DISCONNECTED, AgentEventType.AGENT_CLOSED_BY_SERVER,
            SocketStateCode.CLOSED_BY_SERVER),

    UNEXPECTED_CLOSE_BY_SERVER(Integer.MAX_VALUE, AgentLifeCycleState.DISCONNECTED, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER,
            SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER, SocketStateCode.ERROR_UNKNOWN,
            SocketStateCode.ERROR_ILLEGAL_STATE_CHANGE, SocketStateCode.ERROR_SYNC_STATE_SESSION);


    private static final EnumSet<ManagedAgentLifeCycle> CLOSED_EVENT
            = EnumSet.of(CLOSED_BY_CLIENT, UNEXPECTED_CLOSE_BY_CLIENT, CLOSED_BY_SERVER, UNEXPECTED_CLOSE_BY_SERVER);

    private static final EnumSet<ManagedAgentLifeCycle> ALL = EnumSet.allOf(ManagedAgentLifeCycle.class);

    private final int eventCounter;
    private final Set<SocketStateCode> managedStateCodeSet;
    private final AgentLifeCycleState agentLifeCycleState;
    private final AgentEventType agentEventType;

    ManagedAgentLifeCycle(int eventCounter, AgentLifeCycleState agentLifeCycleState, AgentEventType agentEventType, SocketStateCode... managedStateCodes) {
        this.eventCounter = eventCounter;
        this.agentLifeCycleState = agentLifeCycleState;
        this.agentEventType = agentEventType;
        this.managedStateCodeSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(managedStateCodes)));
    }

    public int getEventCounter() {
        return this.eventCounter;
    }

    public Set<SocketStateCode> getManagedStateCodes() {
        return this.managedStateCodeSet;
    }

    public AgentLifeCycleState getMappedState() {
        return this.agentLifeCycleState;
    }

    public AgentEventType getMappedEvent() {
        return agentEventType;
    }

    public static ManagedAgentLifeCycle getManagedAgentLifeCycleByStateCode(SocketStateCode stateCode) {
        for (ManagedAgentLifeCycle agentLifeCycle : ALL) {
            if (agentLifeCycle.managedStateCodeSet.contains(stateCode)) {
                return agentLifeCycle;
            }
        }
        return null;
    }

    public boolean isClosedEvent() {
        return CLOSED_EVENT.contains(this);
    }
}