package com.navercorp.pinpoint.web.vo.agent;

import java.util.Objects;

public class AgentStatusFilterChain implements AgentStatusFilter {
    private final AgentStatusFilter[] agentStatusFilters;

    public AgentStatusFilterChain(AgentStatusFilter... agentStatusFilters) {
        this.agentStatusFilters = Objects.requireNonNull(agentStatusFilters, "agentFilters");
    }

    @Override
    public boolean filter(AgentStatus agentStatus) {
        for (AgentStatusFilter agentFilter : this.agentStatusFilters) {
            if (agentFilter.filter(agentStatus) == REJECT) {
                return REJECT;
            }
        }
        return ACCEPT;
    }
}
