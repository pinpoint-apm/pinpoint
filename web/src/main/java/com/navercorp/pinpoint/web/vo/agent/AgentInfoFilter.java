package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

public interface AgentInfoFilter {
    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean filter(AgentAndStatus agentInfo);

    static boolean accept(AgentAndStatus agentAndStatus) {
        return ACCEPT;
    }

    static boolean reject(AgentAndStatus agentAndStatus) {
        return REJECT;
    }

    static boolean filterRunning(AgentAndStatus agentAndStatus) {
        final AgentStatus agentStatus = agentAndStatus.getStatus();
        if (agentStatus == null) {
            return REJECT;
        }
        if (agentStatus.getState() == AgentLifeCycleState.RUNNING) {
            return ACCEPT;
        }
        return REJECT;
    }
}
