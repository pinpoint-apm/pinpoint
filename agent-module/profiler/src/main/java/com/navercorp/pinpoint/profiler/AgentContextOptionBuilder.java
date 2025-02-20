package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.ObjectNameValidationFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentContextOptionBuilder {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static AgentContextOption build(AgentOption agentOption,
                                           ObjectName objectName,
                                           ProfilerConfig profilerConfig) {
        return new DefaultAgentContextOption(
                agentOption.getInstrumentation(),
                objectName,
                profilerConfig,
                agentOption.getAgentPath(),
                agentOption.getPluginJars(),
                agentOption.getBootstrapJarPaths(),
                agentOption.isStaticResourceCleanup());
    }

    public AgentContextOption build(AgentOption agentOption, ProfilerConfig profilerConfig) {
        try {
            ObjectNameBuilder objectNameBuilder = new ObjectNameBuilder();
            ObjectName objectName = objectNameBuilder.build(agentOption, profilerConfig);
            return build(agentOption, objectName, profilerConfig);
        } catch (ObjectNameValidationFailedException e) {
            logger.warn("ObjectName validation failed {}", e.getAgentIdType(), e);
            throw e;
        }

    }

}
