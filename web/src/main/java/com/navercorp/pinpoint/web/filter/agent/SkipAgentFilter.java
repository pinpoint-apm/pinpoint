package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public class SkipAgentFilter implements AgentFilter {

    public static final AgentFilter SKIP_FILTER = new SkipAgentFilter();

    public SkipAgentFilter() {
    }

    @Override
    public boolean accept(String fromAgent, String toAgent) {
        return ACCEPT;
    }

    @Override
    public boolean acceptFrom(String fromAgent) {
        return ACCEPT;
    }

    @Override
    public boolean acceptTo(String toAgent) {
        return ACCEPT;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SkipAgentFilter{");
        sb.append('}');
        return sb.toString();
    }

}
