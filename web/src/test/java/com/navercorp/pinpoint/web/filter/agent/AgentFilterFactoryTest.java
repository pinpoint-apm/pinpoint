package com.navercorp.pinpoint.web.filter.agent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class AgentFilterFactoryTest {


    @Test
    public void fromFilter() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", "b");
        AgentFilter filter = factory.createFromAgentFilter();

        Assert.assertTrue(filter.accept("a"));
        Assert.assertFalse(filter.accept("b"));
        Assert.assertFalse(filter.accept(null));
    }

    @Test
    public void fromFilter_skip() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory(null, "b");
        AgentFilter filter = factory.createFromAgentFilter();

        Assert.assertTrue(filter.accept("a"));
        Assert.assertTrue(filter.accept("b"));
        Assert.assertTrue(filter.accept(null));
    }

    @Test
    public void toFilter() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", "b");
        AgentFilter filter = factory.createToAgentFilter();

        Assert.assertTrue(filter.accept("b"));

        Assert.assertFalse(filter.accept("a"));
        Assert.assertFalse(filter.accept(null));

        Assert.assertTrue(factory.toAgentExist());
    }

    @Test
    public void toFilter_skip() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", null);
        AgentFilter filter = factory.createToAgentFilter();

        Assert.assertTrue(filter.accept("b"));
        Assert.assertTrue(filter.accept("a"));
        Assert.assertTrue(filter.accept(null));

    }



}