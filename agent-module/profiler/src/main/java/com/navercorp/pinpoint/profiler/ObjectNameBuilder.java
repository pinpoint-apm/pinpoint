package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.name.IdSourceType;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolver;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolverBuilder;

import java.util.function.Function;

public class ObjectNameBuilder {

    public ObjectName build(AgentOption agentOption, ProfilerConfig profilerConfig) {
        final String v4 = profilerConfig.readString("pinpoint.v4.enable", "false");
        if (v4.equalsIgnoreCase("true")) {
            return resolveObjectNameV4(agentOption.getAgentArgs()::get);
        } else {
            return resolveObjectNameV1(agentOption.getAgentArgs()::get);
        }
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
