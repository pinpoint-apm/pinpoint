package com.navercorp.pinpoint.bootstrap;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.*;

public class AgentIdResolverTest {

    @Test
    public void resolve() {
        Properties properties = new Properties();
        properties.setProperty(AgentIdResolver.AGENT_ID, "agentId");
        properties.setProperty(AgentIdResolver.APPLICATION_NAME, "appName");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties, AgentIdResolver.AGENT_ID, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap));
        AgentIds resolve = resolver.resolve();

        Assert.assertEquals("agentId", resolve.getAgentId());
        Assert.assertEquals("appName", resolve.getApplicationName());
    }

    @Test
    public void resolve_fail() {
        Properties properties = new Properties();
        properties.setProperty(AgentIdResolver.AGENT_ID, "agentId");

        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties, AgentIdResolver.AGENT_ID, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap));
        AgentIds resolve = resolver.resolve();

        Assert.assertNull(resolve);
    }

    @Test
    public void resolve_multi_source() {
        Properties properties1 = new Properties();
        properties1.setProperty(AgentIdResolver.AGENT_ID, "agentId1");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties1, AgentIdResolver.AGENT_ID, AgentIdResolver.APPLICATION_NAME);

        Properties properties2 = new Properties();
        properties2.setProperty(AgentIdResolver.APPLICATION_NAME, "appName2");
        AgentProperties ap2 = new AgentProperties(AgentIdSourceType.SYSTEM, properties2, AgentIdResolver.AGENT_ID, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap, ap2));
        AgentIds resolve = resolver.resolve();

        Assert.assertEquals("agentId1", resolve.getAgentId());
        Assert.assertEquals("appName2", resolve.getApplicationName());
    }

    @Test
    public void resolve_multi_source_2() {
        Properties properties1 = new Properties();
        properties1.setProperty(AgentIdResolver.AGENT_ID, "agentId1");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties1, AgentIdResolver.AGENT_ID, AgentIdResolver.APPLICATION_NAME);

        Properties properties2 = new Properties();
        properties2.setProperty(AgentIdResolver.AGENT_ID, "agentId2");
        properties2.setProperty(AgentIdResolver.APPLICATION_NAME, "appName2");

        AgentProperties ap2 = new AgentProperties(AgentIdSourceType.SYSTEM, properties2, AgentIdResolver.AGENT_ID, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap, ap2));
        AgentIds resolve = resolver.resolve();

        Assert.assertEquals("agentId2", resolve.getAgentId());
        Assert.assertEquals("appName2", resolve.getApplicationName());
    }
}