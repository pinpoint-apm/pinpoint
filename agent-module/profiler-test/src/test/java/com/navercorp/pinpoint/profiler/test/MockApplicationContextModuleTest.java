/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.test;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.AgentContextOption;
import com.navercorp.pinpoint.profiler.AgentContextOptionBuilder;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.AgentOption;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.v1.ObjectNameV1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Collections;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockApplicationContextModuleTest {

    @Test
    public void test() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();

        Instrumentation instrumentation = Mockito.mock(Instrumentation.class);

        AgentOption agentOption = new AgentOption(instrumentation,
                profilerConfig.getProperties(), Collections.emptyMap(), null,
                Collections.emptyList(), Collections.emptyList(), false);

        try (PluginTestAgent pluginTestAgent = new PluginTestAgent(agentOption.toMap())) {
            pluginTestAgent.start();
        }
    }

    @Test
    public void testMockApplicationContext() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        Instrumentation instrumentation = Mockito.mock(Instrumentation.class);

        AgentOption agentOption = new AgentOption(instrumentation,
                profilerConfig.getProperties(), Collections.emptyMap(), null,
                Collections.emptyList(), Collections.emptyList(), false);

        Module pluginModule = new PluginApplicationContextModule();
        ModuleFactory moduleFactory = new OverrideModuleFactory(pluginModule);
        ObjectName objectName = new ObjectNameV1("mockAgentId", "mockAgentName", "mockApplicationName");

        AgentContextOption agentContextOption = AgentContextOptionBuilder.build(agentOption,
                objectName, profilerConfig);
        try (DefaultApplicationContext applicationContext = new DefaultApplicationContext(agentContextOption, moduleFactory)) {

            Injector injector = applicationContext.getInjector();
            // singleton check
            AgentInfoSender instance1 = injector.getInstance(AgentInfoSender.class);
            AgentInfoSender instance2 = injector.getInstance(AgentInfoSender.class);
            Assertions.assertSame(instance1, instance2);

            ClassFileTransformer instance4 = injector.getInstance(ClassFileTransformer.class);

        }
    }


}
