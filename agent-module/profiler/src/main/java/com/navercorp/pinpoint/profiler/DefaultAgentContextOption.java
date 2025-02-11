package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.name.ObjectName;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class DefaultAgentContextOption implements AgentContextOption {
    private final Instrumentation instrumentation;

    private final ObjectName objectName;

    private final ProfilerConfig profilerConfig;

    private final Path agentPath;
    private final List<Path> pluginJars;
    private final List<Path> bootstrapJarPaths;

    private final boolean staticResourceCleanup;

    public DefaultAgentContextOption(final Instrumentation instrumentation,
                                     final ObjectName objectName,
                                     final ProfilerConfig profilerConfig,
                                     final Path agentPath,
                                     final List<Path> pluginJars,
                                     final List<Path> bootstrapJarPaths,
                                     boolean staticResourceCleanup) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.objectName = Objects.requireNonNull(objectName, "objectName");
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");

        this.agentPath = agentPath;
        this.pluginJars = Objects.requireNonNull(pluginJars, "pluginJars");
        this.bootstrapJarPaths = Objects.requireNonNull(bootstrapJarPaths, "bootstrapJarPaths");
        this.staticResourceCleanup = staticResourceCleanup;
    }

    @Override
    public Instrumentation getInstrumentation() {
        return this.instrumentation;
    }

    @Override
    public ObjectName getObjectName() {
        return objectName;
    }

    @Override
    public Path getAgentPath() {
        return agentPath;
    }

    @Override
    public List<Path> getPluginJars() {
        return this.pluginJars;
    }

    @Override
    public List<Path> getBootstrapJarPaths() {
        return this.bootstrapJarPaths;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    @Override
    public boolean getStaticResourceCleanup() {
        return staticResourceCleanup;
    }

    @Override
    public String toString() {
        return "DefaultAgentOption{" +
                "instrumentation=" + instrumentation +
                ", objectName='" + objectName + '\'' +
                ", profilerConfig=" + profilerConfig +
                ", pluginJars=" + pluginJars +
                ", bootstrapJarPaths=" + bootstrapJarPaths +
                ", staticResourceCleanup=" + staticResourceCleanup +
                '}';
    }
}
