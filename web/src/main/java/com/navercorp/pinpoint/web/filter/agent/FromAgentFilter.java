package com.navercorp.pinpoint.web.filter.agent;

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
    public boolean accept(String fromAgent, String toAgent) {
        return filerFrom(fromAgent);
    }

    private boolean filerFrom(String formAgent) {
        if (this.fromAgent.equals(formAgent)) {
            return ACCEPT;
        }
        return REJECT;
    }

    @Override
    public boolean acceptFrom(String fromAgent) {
        return filerFrom(fromAgent);
    }

    @Override
    public boolean acceptTo(String toAgent) {
        return ACCEPT;
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FromAgentFilter{");
        sb.append("fromAgent='").append(fromAgent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
