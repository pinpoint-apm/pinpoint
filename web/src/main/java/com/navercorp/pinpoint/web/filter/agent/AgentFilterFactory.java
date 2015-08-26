package com.navercorp.pinpoint.web.filter.agent;

import org.apache.commons.lang3.StringUtils;

/**
 * @author emeroad
 */
public class AgentFilterFactory {

    private final String fromAgent;
    private final String toAgent;

    public AgentFilterFactory(String fromAgent, String toAgent) {
        this.fromAgent = fromAgent;
        this.toAgent = toAgent;
    }

    public AgentFilter createFilter() {

        if (isNotBlank(fromAgent) && isNotBlank(toAgent)) {
            return new FromToAgentFilter(fromAgent, toAgent);
        }
        if (isNotBlank(fromAgent)) {
            return new FromAgentFilter(fromAgent);
        }
        if (isNotBlank(toAgent)) {
            return new ToAgentFilter(toAgent);
        }
        return SkipAgentFilter.SKIP_FILTER;
    }

    private static boolean isNotBlank(String toAgent) {
        return StringUtils.isNotBlank(toAgent);
    }

}
