package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

public class DefaultAgentInfoFilter implements AgentInfoFilter {
    private final long from;

    public DefaultAgentInfoFilter(long from) {
        this.from = from;
    }

    @Override
    public boolean filter(AgentAndStatus agentAndStatus) {
        final AgentStatus agentStatus = agentAndStatus.getStatus();
        if (agentStatus == null) {
            return REJECT;
        }
        if (agentStatus.getState() == AgentLifeCycleState.RUNNING) {
            return ACCEPT;
        }
        if (agentStatus.getEventTimestamp() >= from) {
            return ACCEPT;
        }
        return REJECT;
    }
}
