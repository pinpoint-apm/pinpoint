package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.name.ObjectName;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.List;

public interface AgentContextOption {

    Instrumentation getInstrumentation();

    ObjectName getObjectName();

    ProfilerConfig getProfilerConfig();

    Path getAgentPath();

    List<Path> getPluginJars();

    List<Path> getBootstrapJarPaths();

    boolean getStaticResourceCleanup();
}
