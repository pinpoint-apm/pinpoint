package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public class DefaultSimpleAgentFilter implements SimpleAgentFilter {
    private final String agentId;

    public DefaultSimpleAgentFilter(String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
    }

    public boolean accept(String agentId) {
        return this.agentId.equals(agentId);
    }
}
