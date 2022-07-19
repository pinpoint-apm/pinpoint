package com.navercorp.pinpoint.web.vo;

import java.util.Objects;

public class AgentInfoFilterChain implements AgentInfoFilter {
    private final AgentInfoFilter[] agentInfoFilters;

    public AgentInfoFilterChain(AgentInfoFilter... agentInfoFilters) {
        this.agentInfoFilters = Objects.requireNonNull(agentInfoFilters, "agentFilters");
    }

    @Override
    public boolean filter(AgentAndStatus agentAndStatus) {
        for (AgentInfoFilter agentFilter : this.agentInfoFilters) {
            if (agentFilter.filter(agentAndStatus) == ACCEPT) {
                return ACCEPT;
            }
        }
        return REJECT;
    }
}
