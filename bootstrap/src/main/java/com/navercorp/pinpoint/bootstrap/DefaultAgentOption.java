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
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;

/**
 * @author emeroad
 */
public class DefaultAgentOption implements AgentOption {

    private final Instrumentation instrumentation;

    private final String agentId;
    private final String applicationName;

    private final ProfilerConfig profilerConfig;
    private final URL[] pluginJars;
    private final List<String> bootstrapJarPaths;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    public DefaultAgentOption(final Instrumentation instrumentation, String agentId, String applicationName, final ProfilerConfig profilerConfig, final URL[] pluginJars, final List<String> bootstrapJarPaths, final ServiceTypeRegistryService serviceTypeRegistryService, final AnnotationKeyRegistryService annotationKeyRegistryService) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (pluginJars == null) {
            throw new NullPointerException("pluginJars must not be null");
        }
        if (annotationKeyRegistryService == null) {
            throw new NullPointerException("annotationKeyRegistryService must not be null");
        }
        if (serviceTypeRegistryService == null) {
            throw new NullPointerException("serviceTypeRegistryService must not be null");
        }
        this.instrumentation = instrumentation;
        this.agentId = agentId;
        this.applicationName = applicationName;
        this.profilerConfig = profilerConfig;
        this.pluginJars = pluginJars;
        if (bootstrapJarPaths == null) {
            this.bootstrapJarPaths = Collections.emptyList();
        } else {
            this.bootstrapJarPaths = bootstrapJarPaths;
        }
        this.serviceTypeRegistryService = serviceTypeRegistryService;
        this.annotationKeyRegistryService = annotationKeyRegistryService;
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
    public URL[] getPluginJars() {
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
    public ServiceTypeRegistryService getServiceTypeRegistryService() {
        return this.serviceTypeRegistryService;
    }

    @Override
    public AnnotationKeyRegistryService getAnnotationKeyRegistryService() {
        return this.annotationKeyRegistryService;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultAgentOption{");
        sb.append("instrumentation=").append(instrumentation);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", applicationName='").append(applicationName).append('\'');
        sb.append(", profilerConfig=").append(profilerConfig);
        sb.append(", pluginJars=").append(Arrays.toString(pluginJars));
        sb.append(", bootstrapJarPaths=").append(bootstrapJarPaths);
        sb.append(", serviceTypeRegistryService=").append(serviceTypeRegistryService);
        sb.append(", annotationKeyRegistryService=").append(annotationKeyRegistryService);
        sb.append('}');
        return sb.toString();
    }
}
