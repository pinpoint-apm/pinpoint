package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public class SkipSimpleAgentFilter implements SimpleAgentFilter {
    public static final SimpleAgentFilter SKIP_FILTER = new SkipSimpleAgentFilter();

    public SkipSimpleAgentFilter() {
    }

    @Override
    public boolean accept(String agentId) {
        return ACCEPT;
    }
}
