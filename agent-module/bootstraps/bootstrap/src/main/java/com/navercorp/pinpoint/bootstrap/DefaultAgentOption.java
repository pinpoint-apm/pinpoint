/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap;


import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author emeroad
 */
public class DefaultAgentOption implements AgentOption {

    private final Instrumentation instrumentation;

    private final Properties properties;
    private final Map<String, String> agentArgs;

    private final List<Path> pluginJars;
    private final List<Path> bootstrapJarPaths;

    public DefaultAgentOption(final Instrumentation instrumentation,
                              final Properties properties,
                              final Map<String, String> agentArgs,
                              final List<Path> pluginJars,
                              final List<Path> bootstrapJarPaths) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");

        this.properties = Objects.requireNonNull(properties, "properties");
        this.agentArgs = Objects.requireNonNull(agentArgs, "agentArgs");

        this.pluginJars = Objects.requireNonNull(pluginJars, "pluginJars");
        this.bootstrapJarPaths = Objects.requireNonNull(bootstrapJarPaths, "bootstrapJarPaths");
    }

    @Override
    public Instrumentation getInstrumentation() {
        return this.instrumentation;
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
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Map<String, String> getAgentArgs() {
        return agentArgs;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("instrumentation", instrumentation);
        map.put("properties", properties);
        map.put("agentArgs", agentArgs);
        map.put("pluginJars", pluginJars);
        map.put("bootstrapJarPaths", bootstrapJarPaths);
        return map;
    }

    @Override
    public String toString() {
        return "DefaultAgentOption{" +
                "instrumentation=" + instrumentation +
                ", properties=" + properties +
                ", agentArgs=" + agentArgs +
                ", pluginJars=" + pluginJars +
                ", bootstrapJarPaths=" + bootstrapJarPaths +
                '}';
    }
}
