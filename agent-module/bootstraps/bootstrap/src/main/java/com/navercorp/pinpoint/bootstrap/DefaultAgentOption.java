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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.id.AgentId;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultAgentOption implements AgentOption {

    private final Instrumentation instrumentation;

    private final AgentId agentId;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;
    private final boolean isContainer;

    private final ProfilerConfig profilerConfig;
    private final List<String> pluginJars;
    private final List<String> bootstrapJarPaths;

    public DefaultAgentOption(final Instrumentation instrumentation,
                              AgentId agentId, String agentName, String applicationName, String serviceName, final boolean isContainer,
                              final ProfilerConfig profilerConfig, final List<String> pluginJars, final List<String> bootstrapJarPaths) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentName = Objects.requireNonNull(agentName, "agentName");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.isContainer = isContainer;
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.pluginJars = Objects.requireNonNull(pluginJars, "pluginJars");
        this.bootstrapJarPaths = Objects.requireNonNull(bootstrapJarPaths, "bootstrapJarPaths");
    }

    @Override
    public Instrumentation getInstrumentation() {
        return this.instrumentation;
    }

    @Override
    public AgentId getAgentId() {
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
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean isContainer() {
        return isContainer;
    }

    @Override
    public List<String> getPluginJars() {
        return this.pluginJars;
    }

    @Override
    public List<String> getBootstrapJarPaths() {
        return this.bootstrapJarPaths;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return this.profilerConfig;
    }

    @Override
    public String toString() {
        return "DefaultAgentOption{" + "instrumentation=" + instrumentation +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", isContainer=" + isContainer +
                ", profilerConfig=" + profilerConfig +
                ", pluginJars=" + pluginJars +
                ", bootstrapJarPaths=" + bootstrapJarPaths +
                '}';
    }
}
