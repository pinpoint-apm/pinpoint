package com.navercorp.pinpoint.profiler;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class AgentOption {

    private final Instrumentation instrumentation;
    private final Properties properties;
    private final Map<String, String> agentArgs;
    private final Path agentPath;
    private final List<Path> pluginJars;
    private final List<Path> bootstrapJarPaths;
    private final boolean staticResourceCleanup;

    @SuppressWarnings("unchecked")
    public static AgentOption of(Map<String, Object> map) {
        Instrumentation instrumentation = (Instrumentation) map.get("instrumentation");
        Properties properties = (Properties) map.get("properties");
        Map<String, String> agentArgs = (Map<String, String>) map.getOrDefault("agentArgs", Collections.emptyMap());
        Path agentPath = (Path) map.get("agentPath");
        List<Path> pluginJars = (List<Path>) map.getOrDefault("pluginJars", Collections.emptyList());
        List<Path> bootstrapJarPaths = (List<Path>) map.getOrDefault("bootstrapJarPaths", Collections.emptyList());
        return new AgentOption(instrumentation, properties, agentArgs, agentPath, pluginJars, bootstrapJarPaths, true);
    }

    public AgentOption(Instrumentation instrumentation,
                       Properties properties,
                       Map<String, String> agentArgs,
                       Path agentPath,
                       List<Path> pluginJars,
                       List<Path> bootstrapJarPaths,
                       boolean staticResourceCleanup) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.agentArgs = Objects.requireNonNull(agentArgs, "agentArgs");
        this.agentPath = agentPath;
        this.pluginJars = pluginJars;
        this.bootstrapJarPaths = bootstrapJarPaths;
        this.staticResourceCleanup = staticResourceCleanup;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public Properties getProperties() {
        return properties;
    }

    public Map<String, String> getAgentArgs() {
        return agentArgs;
    }

    public Path getAgentPath() {
        return agentPath;
    }

    public List<Path> getPluginJars() {
        return pluginJars;
    }

    public List<Path> getBootstrapJarPaths() {
        return bootstrapJarPaths;
    }

    public boolean isStaticResourceCleanup() {
        return staticResourceCleanup;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("instrumentation", instrumentation);
        map.put("properties", properties);
        map.put("agentArgs", agentArgs);
        map.put("pluginJars", pluginJars);
        map.put("agentPath", agentPath);
        map.put("bootstrapJarPaths", bootstrapJarPaths);
        map.put("staticResourceCleanup", staticResourceCleanup);
        return map;
    }

    @Override
    public String toString() {
        return "AgentOption{" +
                "instrumentation=" + instrumentation +
                ", properties=" + properties +
                ", agentArgs=" + agentArgs +
                ", pluginJars=" + pluginJars +
                ", bootstrapJarPaths=" + bootstrapJarPaths +
                ", staticResourceCleanup=" + staticResourceCleanup +
                '}';
    }
}
