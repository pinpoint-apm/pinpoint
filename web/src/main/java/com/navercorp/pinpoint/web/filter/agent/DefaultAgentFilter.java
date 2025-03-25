package com.navercorp.pinpoint.web.filter.agent;

import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultAgentFilter implements AgentFilter {
    private final String agentId;

    public DefaultAgentFilter(String agentId) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
    }

    public boolean accept(String agentId) {
        if (this.agentId.equals(agentId)) {
            return ACCEPT;
        }
        return REJECT;
    }

    @Override
    public String toString() {
        return "DefaultAgentFilter{" +
                "agentId='" + agentId + '\'' +
                '}';
    }
}
