package com.navercorp.pinpoint.web.filter.agent;

import com.navercorp.pinpoint.web.filter.agent.AgentFilter;

/**
 * @author emeroad
 */
public class FromAgentFilter implements AgentFilter {
    private final String fromAgent;

    public FromAgentFilter(String fromAgent) {
        if (fromAgent == null) {
            throw new NullPointerException("fromAgent must not be null");
        }
        this.fromAgent = fromAgent;
    }

    @Override
    public boolean accept(String formAgent, String toAgent) {
        if (this.fromAgent.equals(formAgent)) {
            return ACCEPT;
        }
        return REJECT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FromAgentFilter{");
        sb.append("fromAgent='").append(fromAgent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
