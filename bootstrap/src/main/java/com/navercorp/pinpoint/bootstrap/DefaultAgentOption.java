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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;

/**
 * @author emeroad
 */
public class DefaultAgentOption implements AgentOption {
    private final String agentArgs;
    private final Instrumentation instrumentation;
    private final ProfilerConfig profilerConfig;
    private final URL[] pluginJars;
    private final String bootStrapJarCorePath;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    public DefaultAgentOption(final String agentArgs, final Instrumentation instrumentation, final ProfilerConfig profilerConfig, final URL[] pluginJars, final String bootStrapJarCorePath, final ServiceTypeRegistryService serviceTypeRegistryService, final AnnotationKeyRegistryService annotationKeyRegistryService) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (pluginJars == null) {
            throw new NullPointerException("pluginJars must not be null");
        }
//        if (bootStrapJarCorePath == null) {
//            throw new NullPointerException("bootStrapJarCorePath must not be null");
//        }
        if (annotationKeyRegistryService == null) {
            throw new NullPointerException("annotationKeyRegistryService must not be null");
        }
        if (serviceTypeRegistryService == null) {
            throw new NullPointerException("serviceTypeRegistryService must not be null");
        }
        this.agentArgs = agentArgs;
        this.instrumentation = instrumentation;
        this.profilerConfig = profilerConfig;
        this.pluginJars = pluginJars;
        this.bootStrapJarCorePath = bootStrapJarCorePath;
        this.serviceTypeRegistryService = serviceTypeRegistryService;
        this.annotationKeyRegistryService = annotationKeyRegistryService;
    }

    @Override
    public String getAgentArgs() {
        return this.agentArgs;
    }

    @Override
    public Instrumentation getInstrumentation() {
        return this.instrumentation;
    }

    @Override
    public URL[] getPluginJars() {
        return this.pluginJars;
    }

    @Override
    public String getBootStrapCoreJarPath() {
        return this.bootStrapJarCorePath;
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
        return "DefaultAgentOption{" +
                "agentArgs='" + agentArgs + '\'' +
                ", instrumentation=" + instrumentation +
                ", profilerConfig=" + profilerConfig +
                ", bootStrapJarCorePath='" + bootStrapJarCorePath +
                '}';
    }
}
