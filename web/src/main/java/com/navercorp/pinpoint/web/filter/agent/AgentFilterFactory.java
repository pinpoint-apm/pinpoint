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

    private boolean isNotBlank(String toAgent) {
        return StringUtils.isNotBlank(toAgent);
    }

    public SimpleAgentFilter createSimpleFromAgentFilter() {
        return createSimpleAgentFilter(fromAgent);
    }

    public SimpleAgentFilter createSimpleToAgentFilter() {
        return createSimpleAgentFilter(toAgent);
    }

    private SimpleAgentFilter createSimpleAgentFilter(String agentId) {
        if (StringUtils.isBlank(agentId)) {
            return SkipSimpleAgentFilter.SKIP_FILTER;
        }
        return new DefaultSimpleAgentFilter(agentId);
    }

    public boolean fromAgentExist() {
        return isNotBlank(fromAgent);
    }

    public boolean toAgentExist() {
        return isNotBlank(toAgent);
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
