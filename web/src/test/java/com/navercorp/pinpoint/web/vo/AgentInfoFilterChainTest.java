package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import org.junit.Assert;
import org.junit.Test;

public class AgentInfoFilterChainTest {

    @Test
    public void filter_server() {

        AgentInfoFilter chain = new AgentInfoFilterChain(
                AgentInfoFilter::filterServer,
                AgentInfoFilter::reject
        );

        AgentInfo info = new AgentInfo();
        info.setContainer(false);

        Assert.assertEquals(AgentInfoFilter.ACCEPT, chain.filter(info));
    }

    @Test
    public void filter_container() {

        AgentInfoFilter chain = new AgentInfoFilterChain(
                AgentInfoFilter::filterServer,
                AgentInfoFilter::reject
        );

        AgentInfo info = new AgentInfo();
        info.setContainer(true);

        Assert.assertEquals(AgentInfoFilter.REJECT, chain.filter(info));
    }

    @Test
    public void filter_from_accept() {
        final long current = System.currentTimeMillis();

        AgentInfoFilter chain = new AgentInfoFilterChain(
                new DefaultAgentInfoFilter(current)
        );

        AgentStatus status = new AgentStatus("testAgent", AgentLifeCycleState.RUNNING, current);
        AgentInfo info = new AgentInfo();
        info.setStatus(status);

        Assert.assertEquals(AgentInfoFilter.ACCEPT, chain.filter(info));
    }

    @Test
    public void filter_from_reject() {
        final long current = System.currentTimeMillis();

        AgentInfoFilter chain = new AgentInfoFilterChain(
                new DefaultAgentInfoFilter(Long.MAX_VALUE)
        );

        AgentStatus status = new AgentStatus("testAgent", AgentLifeCycleState.RUNNING, current);
        AgentInfo info = new AgentInfo();
        info.setStatus(status);

        Assert.assertEquals(AgentInfoFilter.ACCEPT, chain.filter(info));
    }
}