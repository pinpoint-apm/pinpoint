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

import com.google.inject.Injector;
import com.google.inject.Module;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.DefaultAgentOption;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Collections;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockApplicationContextModuleTest {

    @Test
    public void test() {
        ProfilerConfig profilerConfig = spy(new DefaultProfilerConfig());
        when(profilerConfig.getStaticResourceCleanup()).thenReturn(true);
        Instrumentation instrumentation = Mockito.mock(Instrumentation.class);

        AgentOption agentOption = new DefaultAgentOption(instrumentation,
                "mockAgent", "mockApplicationName", false, profilerConfig, Collections.<String>emptyList(),
                null);

        PluginTestAgent pluginTestAgent = new PluginTestAgent(agentOption);
        try {
            pluginTestAgent.start();
        } finally {
            pluginTestAgent.stop();
        }
    }

    @Test
    public void testMockApplicationContext() {
        ProfilerConfig profilerConfig = spy(new DefaultProfilerConfig());
        when(profilerConfig.getStaticResourceCleanup()).thenReturn(true);
        Instrumentation instrumentation = Mockito.mock(Instrumentation.class);

        AgentOption agentOption = new DefaultAgentOption(instrumentation,
                "mockAgent", "mockApplicationName", false, profilerConfig, Collections.<String>emptyList(),
                null);

        Module pluginModule = new PluginApplicationContextModule();
        InterceptorRegistryBinder interceptorRegistryBinder = new TestInterceptorRegistryBinder();
        Module testInterceptorRegistryModule = InterceptorRegistryModule.wrap(interceptorRegistryBinder);
        ModuleFactory moduleFactory = new OverrideModuleFactory(pluginModule, testInterceptorRegistryModule);

        DefaultApplicationContext applicationContext = new DefaultApplicationContext(agentOption, moduleFactory);

        Injector injector = applicationContext.getInjector();
        // singleton check
        AgentInfoSender instance1 = injector.getInstance(AgentInfoSender.class);
        AgentInfoSender instance2 = injector.getInstance(AgentInfoSender.class);
        Assert.assertSame(instance1, instance2);

        ClassFileTransformer instance4 = injector.getInstance(ClassFileTransformer.class);

        applicationContext.close();
    }



}