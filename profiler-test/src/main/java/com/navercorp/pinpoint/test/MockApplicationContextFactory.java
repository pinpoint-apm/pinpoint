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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.Collections;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockApplicationContextFactory {

    public MockApplicationContextFactory() {
    }

    public DefaultApplicationContext build(String configPath) {
        ProfilerConfig profilerConfig = loadProfilerConfig(configPath);
        return build(profilerConfig);
    }

    private ProfilerConfig loadProfilerConfig(String configPath) {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final InputStream resource = classLoader.getResourceAsStream(configPath);
        if (resource == null) {
            throw new RuntimeException("pinpoint.config not found. configPath:" + configPath);
        }

        return ProfilerConfigLoader.load(resource);
    }

    public DefaultApplicationContext build(ProfilerConfig config) {
        DefaultApplicationContext context = build(config, newModuleFactory());
        return context;
    }

    public DefaultApplicationContext build(ProfilerConfig config, ModuleFactory moduleFactory) {
        Instrumentation instrumentation = new DummyInstrumentation();
        String mockAgentId = "mockAgentId";
        String mockAgentName = "mockAgentName";
        String mockApplicationName = "mockApplicationName";

        AgentOption agentOption = new DefaultAgentOption(instrumentation, mockAgentId, mockAgentName, mockApplicationName, false,
                config, Collections.<String>emptyList(), Collections.<String>emptyList());
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
