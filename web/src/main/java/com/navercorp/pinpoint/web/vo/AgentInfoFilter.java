package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

public interface AgentInfoFilter {
    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean filter(AgentInfo agentInfo);

    static boolean accept(AgentInfo agentInfo) {
        return ACCEPT;
    }

    static boolean reject(AgentInfo agentInfo) {
        return REJECT;
    }

    static boolean filterRunning(AgentInfo agentInfo) {
        final AgentStatus agentStatus = agentInfo.getStatus();
        if (agentStatus == null) {
            return REJECT;
        }
        if (agentStatus.getState() == AgentLifeCycleState.RUNNING) {
            return ACCEPT;
        }
        return REJECT;
    }
}
