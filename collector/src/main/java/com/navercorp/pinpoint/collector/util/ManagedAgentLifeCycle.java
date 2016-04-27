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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;

public enum ManagedAgentLifeCycle {
    RUNNING(0, SocketStateCode.RUN_SIMPLEX, SocketStateCode.RUN_DUPLEX),
    CLOSED_BY_CLIENT(Integer.MAX_VALUE, SocketStateCode.CLOSED_BY_CLIENT),
    UNEXPECTED_CLOSE_BY_CLIENT(Integer.MAX_VALUE, SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT),
    CLOSED_BY_SERVER(Integer.MAX_VALUE, SocketStateCode.CLOSED_BY_SERVER),
    UNEXPECTED_CLOSE_BY_SERVER(Integer.MAX_VALUE, SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER, SocketStateCode.ERROR_UNKNOWN,
            SocketStateCode.ERROR_ILLEGAL_STATE_CHANGE, SocketStateCode.ERROR_SYNC_STATE_SESSION);

    private static final EnumMap<ManagedAgentLifeCycle, AgentLifeCycleState> MAPPED_STATE = new EnumMap<>(
            ManagedAgentLifeCycle.class);

    private static final EnumMap<ManagedAgentLifeCycle, AgentEventType> MAPPED_EVENT = new EnumMap<>(
            ManagedAgentLifeCycle.class);

    static {
        MAPPED_STATE.put(RUNNING, AgentLifeCycleState.RUNNING);
        MAPPED_STATE.put(CLOSED_BY_CLIENT, AgentLifeCycleState.SHUTDOWN);
        MAPPED_STATE.put(UNEXPECTED_CLOSE_BY_CLIENT, AgentLifeCycleState.UNEXPECTED_SHUTDOWN);
        MAPPED_STATE.put(CLOSED_BY_SERVER, AgentLifeCycleState.DISCONNECTED);
        MAPPED_STATE.put(UNEXPECTED_CLOSE_BY_SERVER, AgentLifeCycleState.DISCONNECTED);

        MAPPED_EVENT.put(RUNNING, AgentEventType.AGENT_CONNECTED);
        MAPPED_EVENT.put(CLOSED_BY_CLIENT, AgentEventType.AGENT_SHUTDOWN);
        MAPPED_EVENT.put(UNEXPECTED_CLOSE_BY_CLIENT, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN);
        MAPPED_EVENT.put(CLOSED_BY_SERVER, AgentEventType.AGENT_CLOSED_BY_SERVER);
        MAPPED_EVENT.put(UNEXPECTED_CLOSE_BY_SERVER, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER);
    }

    private final int eventCounter;
    private final Set<SocketStateCode> managedStateCodeSet;

    ManagedAgentLifeCycle(int eventCounter, SocketStateCode... managedStateCodes) {
        this.eventCounter = eventCounter;
        this.managedStateCodeSet = new HashSet<>(Arrays.asList(managedStateCodes));
    }

    public int getEventCounter() {
        return this.eventCounter;
    }

    public Set<SocketStateCode> getManagedStateCodes() {
        return Collections.unmodifiableSet(this.managedStateCodeSet);
    }

    public AgentLifeCycleState getMappedState() {
        return MAPPED_STATE.get(this);
    }

    public AgentEventType getMappedEvent() {
        return MAPPED_EVENT.get(this);
    }

    public static ManagedAgentLifeCycle getManagedAgentLifeCycleByStateCode(SocketStateCode stateCode) {
        for (ManagedAgentLifeCycle agentLifeCycle : ManagedAgentLifeCycle.values()) {
            if (agentLifeCycle.managedStateCodeSet.contains(stateCode)) {
                return agentLifeCycle;
            }
        }
        return null;
    }
}