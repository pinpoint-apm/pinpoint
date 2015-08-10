package com.navercorp.pinpoint.web.filter.agent;

import com.navercorp.pinpoint.web.filter.agent.AgentFilter;

/**
 * @author emeroad
 */
public class FromToAgentFilter implements AgentFilter {
    private final String fromAgent;
    private final String toAgent;

    public FromToAgentFilter(String fromAgent, String toAgent) {
        if (fromAgent == null) {
            throw new NullPointerException("fromAgent must not be null");
        }
        if (toAgent == null) {
            throw new NullPointerException("toAgent must not be null");
        }
        this.fromAgent = fromAgent;
        this.toAgent = toAgent;
    }

    @Override
    public boolean accept(String formAgent, String toAgent) {
        if (this.fromAgent.equals(formAgent) && this.toAgent.equals(toAgent)) {
            return ACCEPT;
        }
        return REJECT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FormToAgentFilter{");
        sb.append("fromAgent='").append(fromAgent).append('\'');
        sb.append(", toAgent='").append(toAgent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
