package com.navercorp.pinpoint.web.filter.agent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class AgentFilterFactoryTest {

    public AgentFilter createFilter(String from, String to) {
        AgentFilterFactory factory = new AgentFilterFactory(from, to);
        return factory.createFilter();
    }

    @Test
    public void testCreateAgentFilter() throws Exception {
        AgentFilter fromToFilter = createFilter("a", "b");
        Assert.assertTrue(fromToFilter instanceof FromToAgentFilter);

        AgentFilter fromFilter = createFilter("a", null);
        Assert.assertTrue(fromFilter instanceof FromAgentFilter);

        AgentFilter toFilter = createFilter(null, "b");
        Assert.assertTrue(toFilter instanceof ToAgentFilter);
    }

    @Test
    public void fromToFilter() throws Exception {
        AgentFilter filter = createFilter("a", "b");

        Assert.assertTrue(filter.accept("a", "b"));
        Assert.assertFalse(filter.accept("a", "c"));
        Assert.assertFalse(filter.accept("a", null));

    }

    @Test
    public void fromFilter() throws Exception {
        AgentFilter filter = createFilter("a", null);

        Assert.assertTrue(filter.accept("a", "b"));
        Assert.assertTrue(filter.accept("a", "c"));
        Assert.assertTrue(filter.accept("a", null));
        Assert.assertFalse(filter.accept("b", null));

    }

    @Test
    public void toFilter() throws Exception {
        AgentFilter filter = createFilter(null, "b");

        Assert.assertTrue(filter.accept("a", "b"));
        Assert.assertTrue(filter.accept("b", "b"));
        Assert.assertTrue(filter.accept(null, "b"));
        Assert.assertFalse(filter.accept(null, "a"));

    }

    @Test
    public void simpleFromFilter() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", "b");
        SimpleAgentFilter filter = factory.createSimpleFromAgentFilter();

        Assert.assertTrue(filter.accept("a"));
        Assert.assertFalse(filter.accept("b"));
        Assert.assertFalse(filter.accept(null));
    }

    @Test
    public void simpleFromFilter_skip() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory(null, "b");
        SimpleAgentFilter filter = factory.createSimpleFromAgentFilter();

        Assert.assertTrue(filter.accept("a"));
        Assert.assertTrue(filter.accept("b"));
        Assert.assertTrue(filter.accept(null));
    }

    @Test
    public void simpleToFilter() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", "b");
        SimpleAgentFilter filter = factory.createSimpleToAgentFilter();

        Assert.assertTrue(filter.accept("b"));

        Assert.assertFalse(filter.accept("a"));
        Assert.assertFalse(filter.accept(null));

        Assert.assertTrue(factory.toAgentExist());
    }

    @Test
    public void simpleToFilter_skip() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", null);
        SimpleAgentFilter filter = factory.createSimpleToAgentFilter();

        Assert.assertTrue(filter.accept("b"));
        Assert.assertTrue(filter.accept("a"));
        Assert.assertTrue(filter.accept(null));

    }



}