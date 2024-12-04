package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class DefaultAgentContextOption implements AgentContextOption {
    private final Instrumentation instrumentation;

    private final String agentId;
    private final String agentName;
    private final String applicationName;

    private final ProfilerConfig profilerConfig;

    private final Path agentPath;
    private final List<Path> pluginJars;
    private final List<Path> bootstrapJarPaths;

    private final boolean staticResourceCleanup;

    public DefaultAgentContextOption(final Instrumentation instrumentation,
                                     String agentId,
                                     String agentName,
                                     String applicationName,
                                     final ProfilerConfig profilerConfig,
                                     final Path agentPath,
                                     final List<Path> pluginJars,
                                     final List<Path> bootstrapJarPaths,
                                     boolean staticResourceCleanup) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentName = agentName;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
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
    public String getAgentId() {
        return agentId;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
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
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", profilerConfig=" + profilerConfig +
                ", pluginJars=" + pluginJars +
                ", bootstrapJarPaths=" + bootstrapJarPaths +
                ", staticResourceCleanup=" + staticResourceCleanup +
                '}';
    }
}
