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

import com.navercorp.pinpoint.bootstrap.agentdir.Assert;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class DefaultAgentOption implements AgentOption {

    private final Instrumentation instrumentation;

    private final String agentId;
    private final String applicationName;
    private final boolean isContainer;

    private final ProfilerConfig profilerConfig;
    private final List<String> pluginJars;
    private final List<String> bootstrapJarPaths;

    public DefaultAgentOption(final Instrumentation instrumentation, String agentId, String applicationName, final boolean isContainer, final ProfilerConfig profilerConfig, final List<String> pluginJars, final List<String> bootstrapJarPaths) {
        this.instrumentation = Assert.requireNonNull(instrumentation, "instrumentation");
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.applicationName = Assert.requireNonNull(applicationName, "applicationName");
        this.isContainer = isContainer;
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.pluginJars = Assert.requireNonNull(pluginJars, "pluginJars");
        if (bootstrapJarPaths == null) {
            this.bootstrapJarPaths = Collections.emptyList();
        } else {
            this.bootstrapJarPaths = bootstrapJarPaths;
        }
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
    public String getApplicationName() {
        return applicationName;
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
        final StringBuilder sb = new StringBuilder("DefaultAgentOption{");
        sb.append("instrumentation=").append(instrumentation);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", applicationName='").append(applicationName).append('\'');
        sb.append(", isContainer=").append(isContainer);
        sb.append(", profilerConfig=").append(profilerConfig);
        sb.append(", pluginJars=").append(pluginJars);
        sb.append(", bootstrapJarPaths=").append(bootstrapJarPaths);
        sb.append('}');
        return sb.toString();
    }
}
