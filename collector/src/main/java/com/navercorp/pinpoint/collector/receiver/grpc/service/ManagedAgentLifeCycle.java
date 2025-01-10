package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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

        this.managedStateCodeSet = EnumSet.copyOf(Arrays.asList(managedStateCodes));
    }

    public int getEventCounter() {
        return this.eventCounter;
    }

    public Set<SocketStateCode> getManagedStateCodes() {
        return Collections.unmodifiableSet(this.managedStateCodeSet);
    }

    public AgentLifeCycleState getMappedState() {
        return this.agentLifeCycleState;
    }

    public AgentEventType getMappedEvent() {
        return agentEventType;
    }

    public static ManagedAgentLifeCycle getManagedAgentLifeCycleByStateCode(SocketStateCode stateCode) {
        if (stateCode == null) {
            return null;
        }
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