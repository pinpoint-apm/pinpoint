package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.name.IdSourceType;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolver;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolverBuilder;
import com.navercorp.pinpoint.profiler.name.ObjectNameValidationFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

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
        AgentContextOptionBuilder builder = new AgentContextOptionBuilder();
        try {
            final String v4 = profilerConfig.readString("pinpoint.v4.enable", "false");
            if (v4.equalsIgnoreCase("true")) {
                logger.warn("Use V4 enabled");
                return builder.buildV4(agentOption, profilerConfig);
            } else {
                return builder.buildV1(agentOption, profilerConfig);
            }
        } catch (ObjectNameValidationFailedException e) {
            logger.warn("ObjectName validation failed", e);
            throw e;
        }
    }

    public AgentContextOption buildV1(AgentOption agentOption, ProfilerConfig profilerConfig) {
        final ObjectName objectName = resolveObjectNameV1(agentOption.getAgentArgs()::get);

        return build(agentOption, objectName, profilerConfig);
    }

    public AgentContextOption buildV4(AgentOption agentOption, ProfilerConfig profilerConfig) {
        final ObjectName objectName = resolveObjectNameV4(agentOption.getAgentArgs()::get);

        return build(agentOption, objectName, profilerConfig);
    }

    private void addResolverProperties(ObjectNameResolverBuilder builder, Function<String, String> agentArgs) {
        builder.addProperties(IdSourceType.SYSTEM, System.getProperties()::getProperty);
        builder.addProperties(IdSourceType.SYSTEM_ENV, System.getenv()::get);
        builder.addProperties(IdSourceType.AGENT_ARGUMENT, agentArgs);
    }

    private ObjectName resolveObjectNameV1(Function<String, String> agentArgs) {
        ObjectNameResolverBuilder builder = new ObjectNameResolverBuilder();
        addResolverProperties(builder, agentArgs);

        ObjectNameResolver objectNameResolver = builder.buildV1();
        return objectNameResolver.resolve();
    }

    private ObjectName resolveObjectNameV4(Function<String, String> agentArgs) {
        ObjectNameResolverBuilder builder = new ObjectNameResolverBuilder();
        addResolverProperties(builder, agentArgs);

        ObjectNameResolver objectNameResolver = builder.buildV4();
        return objectNameResolver.resolve();
    }
}
