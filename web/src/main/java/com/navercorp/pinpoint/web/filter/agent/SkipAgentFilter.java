package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public class SkipAgentFilter implements AgentFilter {

    @Override
    public boolean accept(String formAgent, String toAgent) {
        return ACCEPT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SkipAgentFilter{");
        sb.append('}');
        return sb.toString();
    }
}
