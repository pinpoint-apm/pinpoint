package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class AgentContextOptionBuilder {

    public static AgentContextOption build(AgentOption agentOption,
                                           String agentId,
                                           String agentName,
                                           String applicationName,
                                           ProfilerConfig profilerConfig) {
        return new DefaultAgentContextOption(
                agentOption.getInstrumentation(),
                agentId,
                agentName,
                applicationName,
                profilerConfig,
                agentOption.getAgentPath(),
                agentOption.getPluginJars(),
                agentOption.getBootstrapJarPaths(),
                agentOption.isStaticResourceCleanup());
    }
}
