package com.navercorp.pinpoint.web.filter.agent;

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
    public boolean accept(String fromAgent, String toAgent) {
        if (this.fromAgent.equals(fromAgent) && this.toAgent.equals(toAgent)) {
            return ACCEPT;
        }
        return REJECT;
    }

    @Override
    public boolean acceptFrom(String fromAgent) {
        return this.fromAgent.equals(fromAgent);
    }

    @Override
    public boolean acceptTo(String toAgent) {
        return this.toAgent.equals(toAgent);
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
