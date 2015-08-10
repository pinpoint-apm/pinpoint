package com.navercorp.pinpoint.web.filter.agent;

import com.navercorp.pinpoint.web.filter.agent.AgentFilter;

/**
 * @author emeroad
 */
public class ToAgentFilter implements AgentFilter {
    private final String toAgent;

    public ToAgentFilter(String toAgent) {
        if (toAgent == null) {
            throw new NullPointerException("toAgent must not be null");
        }
        this.toAgent = toAgent;
    }

    @Override
    public boolean accept(String formAgent, String toAgent) {
        if (this.toAgent.equals(toAgent)) {
            return ACCEPT;
        }
        return REJECT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ToAgentFilter{");
        sb.append("toAgent='").append(toAgent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
