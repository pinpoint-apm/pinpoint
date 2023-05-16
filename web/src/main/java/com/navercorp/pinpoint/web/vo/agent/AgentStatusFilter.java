package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

public interface AgentStatusFilter {
    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean filter(AgentStatus agentStatus);

    static boolean accept(AgentStatus agentStatus) {
        return ACCEPT;
    }

    static boolean reject(AgentStatus agentStatus) {
        return REJECT;
    }

    static boolean filterRunning(AgentStatus agentStatus) {
        if (agentStatus == null) {
            return REJECT;
        }
        if (agentStatus.getState() == AgentLifeCycleState.RUNNING) {
            return ACCEPT;
        }
        return REJECT;
    }
}
