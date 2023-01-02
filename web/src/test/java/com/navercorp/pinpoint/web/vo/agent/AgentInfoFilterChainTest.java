package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AgentInfoFilterChainTest {

    @Test
    public void filter_running() {
        final long current = System.currentTimeMillis();

        AgentInfoFilter chain = new AgentInfoFilterChain(
                AgentInfoFilter::filterRunning,
                AgentInfoFilter::accept
        );

        AgentStatus status = new AgentStatus("testAgent", AgentLifeCycleState.RUNNING, current);
        AgentAndStatus agentAndStatus = new AgentAndStatus(new AgentInfo(), status);

        Assertions.assertEquals(AgentInfoFilter.ACCEPT, chain.filter(agentAndStatus));
    }

    @Test
    public void filter_from_accept() {
        final long current = System.currentTimeMillis();

        AgentInfoFilter chain = new AgentInfoFilterChain(
                new DefaultAgentInfoFilter(current)
        );

        AgentStatus status = new AgentStatus("testAgent", AgentLifeCycleState.RUNNING, current);
        AgentAndStatus agentAndStatus = new AgentAndStatus(new AgentInfo(), status);
        Assertions.assertEquals(AgentInfoFilter.ACCEPT, chain.filter(agentAndStatus));
    }

    @Test
    public void filter_from_reject() {
        final long current = System.currentTimeMillis();

        AgentInfoFilter chain = new AgentInfoFilterChain(
                new DefaultAgentInfoFilter(Long.MAX_VALUE)
        );

        AgentStatus status = new AgentStatus("testAgent", AgentLifeCycleState.RUNNING, current);
        AgentAndStatus agentAndStatus = new AgentAndStatus(new AgentInfo(), status);
        Assertions.assertEquals(AgentInfoFilter.ACCEPT, chain.filter(agentAndStatus));
    }
}