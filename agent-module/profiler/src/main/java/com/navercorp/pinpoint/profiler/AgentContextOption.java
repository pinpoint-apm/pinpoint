package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.List;

public interface AgentContextOption {

    Instrumentation getInstrumentation();

    String getAgentId();

    String getAgentName();

    String getApplicationName();

    ProfilerConfig getProfilerConfig();

    Path getAgentPath();

    List<Path> getPluginJars();

    List<Path> getBootstrapJarPaths();

    boolean getStaticResourceCleanup();
}
