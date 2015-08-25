package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public class SkipAgentFilter implements AgentFilter {
    public static final AgentFilter SKIP_FILTER = new SkipAgentFilter();

    public SkipAgentFilter() {
    }

    @Override
    public boolean accept(String agentId) {
        return ACCEPT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SkipAgentFilter{");
        sb.append('}');
        return sb.toString();
    }
}
