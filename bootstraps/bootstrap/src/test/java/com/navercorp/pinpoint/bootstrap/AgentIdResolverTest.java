package com.navercorp.pinpoint.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

public class AgentIdResolverTest {

    @Test
    public void resolve() {
        Properties properties = new Properties();
        properties.setProperty(AgentIdResolver.AGENT_ID, "agentId");
        properties.setProperty(AgentIdResolver.AGENT_NAME, "agentName");
        properties.setProperty(AgentIdResolver.APPLICATION_NAME, "appName");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties, AgentIdResolver.AGENT_ID, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap));
        AgentIds resolve = resolver.resolve();

        Assertions.assertEquals("agentId", resolve.getAgentId());
        Assertions.assertEquals("agentName", resolve.getAgentName());
        Assertions.assertEquals("appName", resolve.getApplicationName());
    }

    @Test
    public void resolve_optional_agent_name() {
        Properties properties = new Properties();
        properties.setProperty(AgentIdResolver.AGENT_ID, "agentId");
        properties.setProperty(AgentIdResolver.APPLICATION_NAME, "appName");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties, AgentIdResolver.AGENT_ID, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap));
        AgentIds resolve = resolver.resolve();

        Assertions.assertEquals("agentId", resolve.getAgentId());
        Assertions.assertEquals("appName", resolve.getApplicationName());
        Assertions.assertEquals("", resolve.getAgentName());
    }

    @Test
    public void resolve_fail() {
        Properties properties = new Properties();
        properties.setProperty(AgentIdResolver.AGENT_ID, "agentId");

        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties, AgentIdResolver.AGENT_ID, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap));
        AgentIds resolve = resolver.resolve();

        Assertions.assertNull(resolve);
    }

    @Test
    public void resolve_multi_source() {
        Properties properties1 = new Properties();
        properties1.setProperty(AgentIdResolver.AGENT_ID, "agentId1");
        properties1.setProperty(AgentIdResolver.AGENT_NAME, "agentName1");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties1, AgentIdResolver.AGENT_ID, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME);

        Properties properties2 = new Properties();
        properties2.setProperty(AgentIdResolver.APPLICATION_NAME, "appName2");
        AgentProperties ap2 = new AgentProperties(AgentIdSourceType.SYSTEM, properties2, AgentIdResolver.AGENT_ID, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap, ap2));
        AgentIds resolve = resolver.resolve();

        Assertions.assertEquals("agentId1", resolve.getAgentId());
        Assertions.assertEquals("appName2", resolve.getApplicationName());
        Assertions.assertEquals("agentName1", resolve.getAgentName());
    }

    @Test
    public void resolve_multi_source_2() {
        Properties properties1 = new Properties();
        properties1.setProperty(AgentIdResolver.AGENT_ID, "agentId1");
        properties1.setProperty(AgentIdResolver.AGENT_NAME, "agentName1");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties1, AgentIdResolver.AGENT_ID, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME);

        Properties properties2 = new Properties();
        properties2.setProperty(AgentIdResolver.AGENT_ID, "agentId2");
        properties2.setProperty(AgentIdResolver.AGENT_NAME, "agentName2");
        properties2.setProperty(AgentIdResolver.APPLICATION_NAME, "appName2");

        AgentProperties ap2 = new AgentProperties(AgentIdSourceType.SYSTEM, properties2, AgentIdResolver.AGENT_ID, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap, ap2));
        AgentIds resolve = resolver.resolve();

        Assertions.assertEquals("agentId2", resolve.getAgentId());
        Assertions.assertEquals("appName2", resolve.getApplicationName());
        Assertions.assertEquals("agentName2", resolve.getAgentName());
    }
}