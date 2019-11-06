/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test;

import com.google.inject.Module;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.DefaultAgentOption;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Collections;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockApplicationContextFactory {

    public MockApplicationContextFactory() {
    }

    public DefaultApplicationContext build(String configPath) {
        ProfilerConfig profilerConfig = readProfilerConfig(configPath, this.getClass().getClassLoader());
        return build(profilerConfig);
    }

    private ProfilerConfig readProfilerConfig(String configPath, ClassLoader classLoader) {
        final String path = getFilePath(classLoader, configPath);
        ProfilerConfig profilerConfig = loadProfilerConfig(path);

        String applicationServerType = profilerConfig.getApplicationServerType();
        if (StringUtils.isEmpty(applicationServerType)) {
            applicationServerType = ServiceType.TEST_STAND_ALONE.getName();
        }
        ((DefaultProfilerConfig)profilerConfig).setApplicationServerType(applicationServerType);
        return profilerConfig;
    }

    private ProfilerConfig loadProfilerConfig(String path) {
        try {
            return DefaultProfilerConfig.load(path);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private String getFilePath(ClassLoader classLoader, String configPath) {
        final URL resource = classLoader.getResource(configPath);
        if (resource == null) {
            throw new RuntimeException("pinpoint.config not found. configPath:" + configPath);
        }
        return resource.getPath();
    }

    public DefaultApplicationContext build(ProfilerConfig config) {
        DefaultApplicationContext context = build(config, newModuleFactory());
        return context;
    }

    public DefaultApplicationContext build(ProfilerConfig config, ModuleFactory moduleFactory) {
        Instrumentation instrumentation = new DummyInstrumentation();
        String mockAgent = "mockAgent";
        String mockApplicationName = "mockApplicationName";

        AgentOption agentOption = new DefaultAgentOption(instrumentation, mockAgent, mockApplicationName, false, config, Collections.<String>emptyList(),
                null);
        return new DefaultApplicationContext(agentOption, moduleFactory);
    }


    private ModuleFactory newModuleFactory() {
        Module pluginModule = new MockApplicationContextModule();

        InterceptorRegistryBinder binder = new TestInterceptorRegistryBinder();
        Module interceptorRegistryModule = InterceptorRegistryModule.wrap(binder);
        ModuleFactory moduleFactory = new OverrideModuleFactory(pluginModule, interceptorRegistryModule);
        return moduleFactory;

    }
}
