package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

public class DefaultAgentStatusFilter implements AgentStatusFilter {
    private final long from;

    public DefaultAgentStatusFilter(long from) {
        this.from = from;
    }

    @Override
    public boolean filter(AgentStatus agentStatus) {
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
