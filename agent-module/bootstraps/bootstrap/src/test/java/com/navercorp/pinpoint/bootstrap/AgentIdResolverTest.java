package com.navercorp.pinpoint.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AgentIdResolverTest {

    @Test
    public void resolve() {
        AgentIdSourceType argument = AgentIdSourceType.AGENT_ARGUMENT;
        Map<String, String> properties = new HashMap<>();
        properties.put(argument.getAgentId(), "agentId");
        properties.put(argument.getAgentName(), "agentName");
        properties.put(argument.getApplicationName(), "appName");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties::get);

        AgentIdResolver resolver = new AgentIdResolver(Collections.singletonList(ap));
        AgentIds resolve = resolver.resolve();

        Assertions.assertEquals("agentId", resolve.getAgentId());
        Assertions.assertEquals("agentName", resolve.getAgentName());
        Assertions.assertEquals("appName", resolve.getApplicationName());
    }

    @Test
    public void resolve_optional_agent_name() {
        AgentIdSourceType argument = AgentIdSourceType.AGENT_ARGUMENT;

        Map<String, String> properties = new HashMap<>();
        properties.put(argument.getAgentId(), "agentId");
        properties.put(argument.getApplicationName(), "appName");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties::get);

        AgentIdResolver resolver = new AgentIdResolver(Collections.singletonList(ap));
        AgentIds resolve = resolver.resolve();

        Assertions.assertEquals("agentId", resolve.getAgentId());
        Assertions.assertEquals("appName", resolve.getApplicationName());
        Assertions.assertEquals("", resolve.getAgentName());
    }

    @Test
    public void resolve_fail() {
        AgentIdSourceType argument = AgentIdSourceType.AGENT_ARGUMENT;

        Map<String, String> properties = new HashMap<>();
        properties.put(argument.getAgentId(), "agentId");

        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties::get);

        AgentIdResolver resolver = new AgentIdResolver(Collections.singletonList(ap));
        AgentIds resolve = resolver.resolve();

        Assertions.assertNull(resolve);
    }

    @Test
    public void resolve_multi_source() {
        AgentIdSourceType argument = AgentIdSourceType.AGENT_ARGUMENT;
        Map<String, String> properties1 = new HashMap<>();
        properties1.put(argument.getAgentId(), "agentId1");
        properties1.put(argument.getAgentName(), "agentName1");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties1::get);

        AgentIdSourceType system = AgentIdSourceType.SYSTEM;
        Map<String, String> properties2 = new HashMap<>();
        properties2.put(system.getApplicationName(), "appName2");
        AgentProperties ap2 = new AgentProperties(AgentIdSourceType.SYSTEM, properties2::get);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap2, ap));
        AgentIds resolve = resolver.resolve();

        Assertions.assertEquals("agentId1", resolve.getAgentId());
        Assertions.assertEquals("appName2", resolve.getApplicationName());
        Assertions.assertEquals("agentName1", resolve.getAgentName());
    }

    @Test
    public void resolve_multi_source_2() {
        AgentIdSourceType argument = AgentIdSourceType.AGENT_ARGUMENT;

        Map<String, String> properties1 = new HashMap<>();
        properties1.put(argument.getAgentId(), "agentId1");
        properties1.put(argument.getAgentName(), "agentName1");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties1::get);

        AgentIdSourceType system = AgentIdSourceType.SYSTEM;
        Map<String, String> properties2 = new HashMap<>();
        properties2.put(system.getAgentId(), "agentId2");
        properties2.put(system.getAgentName(), "agentName2");
        properties2.put(system.getApplicationName(), "appName2");

        AgentProperties ap2 = new AgentProperties(AgentIdSourceType.SYSTEM, properties2::get);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap2, ap));
        AgentIds resolve = resolver.resolve();

        Assertions.assertEquals("agentId2", resolve.getAgentId());
        Assertions.assertEquals("appName2", resolve.getApplicationName());
        Assertions.assertEquals("agentName2", resolve.getAgentName());
    }
}