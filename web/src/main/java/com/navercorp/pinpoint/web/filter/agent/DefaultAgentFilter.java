package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public class DefaultAgentFilter implements AgentFilter {
    private final String agentId;

    public DefaultAgentFilter(String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
    }

    public boolean accept(String agentId) {
        if (this.agentId.equals(agentId)) {
            return ACCEPT;
        }
        return REJECT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultAgentFilter{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
