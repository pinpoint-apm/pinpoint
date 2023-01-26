package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AgentStatusFilterChainTest {

    @Test
    public void filter_running() {
        final long current = System.currentTimeMillis();

        AgentStatusFilter chain = new AgentStatusFilterChain(
                AgentStatusFilter::filterRunning,
                AgentStatusFilter::accept
        );

        AgentStatus status = new AgentStatus("testAgent", AgentLifeCycleState.RUNNING, current);

        Assertions.assertEquals(AgentStatusFilter.ACCEPT, chain.filter(status));
    }

    @Test
    public void filter_from_accept() {
        final long current = System.currentTimeMillis();

        AgentStatusFilter chain = new AgentStatusFilterChain(
                new DefaultAgentStatusFilter(current)
        );

        AgentStatus status = new AgentStatus("testAgent", AgentLifeCycleState.RUNNING, current);
        Assertions.assertEquals(AgentStatusFilter.ACCEPT, chain.filter(status));
    }

    @Test
    public void filter_from_reject() {
        final long current = System.currentTimeMillis();

        AgentStatusFilter chain = new AgentStatusFilterChain(
                new DefaultAgentStatusFilter(Long.MAX_VALUE)
        );

        AgentStatus status = new AgentStatus("testAgent", AgentLifeCycleState.RUNNING, current);
        Assertions.assertEquals(AgentStatusFilter.ACCEPT, chain.filter(status));
    }
}