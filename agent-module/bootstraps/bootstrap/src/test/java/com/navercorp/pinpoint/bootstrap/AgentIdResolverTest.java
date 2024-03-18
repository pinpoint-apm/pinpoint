package com.navercorp.pinpoint.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class AgentIdResolverTest {

    @Test
    public void resolve() {
        Properties properties = new Properties();
        properties.setProperty(AgentIdResolver.AGENT_NAME, "agentName");
        properties.setProperty(AgentIdResolver.APPLICATION_NAME, "appName");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME, AgentIdResolver.SERVICE_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Collections.singletonList(ap));
        AgentIds resolve = resolver.resolve();

        assertThat(resolve.getAgentName()).isEqualTo("agentName");
        assertThat(resolve.getApplicationName()).isEqualTo("appName");
    }

    @Test
    public void resolve_optional_agent_name() {
        Properties properties = new Properties();
        properties.setProperty(AgentIdResolver.APPLICATION_NAME, "appName");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME, AgentIdResolver.SERVICE_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Collections.singletonList(ap));
        AgentIds resolve = resolver.resolve();

        assertThat(resolve.getApplicationName()).isEqualTo("appName");
        assertThat(resolve.getAgentName()).isEmpty();
    }

    @Test
    public void resolve_fail() {
        Properties properties = new Properties();

        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME, AgentIdResolver.SERVICE_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Collections.singletonList(ap));
        AgentIds resolve = resolver.resolve();

        assertThat(resolve).isNull();
    }

    @Test
    public void resolve_multi_source() {
        Properties properties1 = new Properties();
        properties1.setProperty(AgentIdResolver.AGENT_NAME, "agentName1");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties1, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME, AgentIdResolver.SERVICE_NAME);

        Properties properties2 = new Properties();
        properties2.setProperty(AgentIdResolver.APPLICATION_NAME, "appName2");
        AgentProperties ap2 = new AgentProperties(AgentIdSourceType.SYSTEM, properties2, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME, AgentIdResolver.SERVICE_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap, ap2));
        AgentIds resolve = resolver.resolve();

        assertThat(resolve.getApplicationName()).isEqualTo("appName2");
        assertThat(resolve.getAgentName()).isEqualTo("agentName1");
    }

    @Test
    public void resolve_multi_source_2() {
        Properties properties1 = new Properties();
        properties1.setProperty(AgentIdResolver.AGENT_NAME, "agentName1");
        AgentProperties ap = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, properties1, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME, AgentIdResolver.SERVICE_NAME);

        Properties properties2 = new Properties();
        properties2.setProperty(AgentIdResolver.AGENT_NAME, "agentName2");
        properties2.setProperty(AgentIdResolver.APPLICATION_NAME, "appName2");

        AgentProperties ap2 = new AgentProperties(AgentIdSourceType.SYSTEM, properties2, AgentIdResolver.AGENT_NAME, AgentIdResolver.APPLICATION_NAME, AgentIdResolver.SERVICE_NAME);

        AgentIdResolver resolver = new AgentIdResolver(Arrays.asList(ap, ap2));
        AgentIds resolve = resolver.resolve();

        assertThat(resolve.getApplicationName()).isEqualTo("appName2");
        assertThat(resolve.getAgentName()).isEqualTo("agentName2");
    }
}