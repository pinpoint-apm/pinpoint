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

    public AgentFilter createFromAgentFilter() {
        return createAgentFilter(fromAgent);
    }

    public AgentFilter createToAgentFilter() {
        return createAgentFilter(toAgent);
    }

    private AgentFilter createAgentFilter(String agentId) {
        if (StringUtils.isBlank(agentId)) {
            return SkipAgentFilter.SKIP_FILTER;
        }
        return new DefaultAgentFilter(agentId);
    }

    public boolean fromAgentExist() {
        return isNotBlank(fromAgent);
    }

    public boolean toAgentExist() {
        return isNotBlank(toAgent);
    }

    private boolean isNotBlank(String toAgent) {
        return StringUtils.isNotBlank(toAgent);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentFilterFactory{");
        sb.append("fromAgent='").append(fromAgent).append('\'');
        sb.append(", toAgent='").append(toAgent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
