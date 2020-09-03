package com.navercorp.pinpoint.web.vo;

import java.util.Objects;

public class AgentInfoFilterChain implements AgentInfoFilter {
    private final AgentInfoFilter[] agentInfoFilters;

    public AgentInfoFilterChain(AgentInfoFilter... agentInfoFilters) {
        this.agentInfoFilters = Objects.requireNonNull(agentInfoFilters, "agentFilters");
    }

    @Override
    public boolean filter(AgentInfo agentInfo) {
        for (AgentInfoFilter agentFilter : this.agentInfoFilters) {
            if (agentFilter.filter(agentInfo) == ACCEPT) {
                return ACCEPT;
            }
        }
        return REJECT;
    }
}
