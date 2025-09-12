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

import com.google.inject.Module;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.profiler.AgentContextOption;
import com.navercorp.pinpoint.profiler.AgentContextOptionBuilder;
import com.navercorp.pinpoint.profiler.AgentOption;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.v1.ObjectNameV1;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockApplicationContextFactory {

    public MockApplicationContextFactory() {
    }

    public DefaultApplicationContext build(String configPath) {
        Properties profilerConfig = loadProfilerConfig(configPath);
        return build(profilerConfig);
    }

    private Properties loadProfilerConfig(String configPath) {
        final InputStream stream = openStream(configPath);
        return ProfilerConfigLoader.loadProperties(stream);
    }

    private InputStream openStream(String configPath) {
        Path file = Paths.get(configPath);
        try {
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("pinpoint.config IOError. configPath:" + configPath);
        }
    }

    public DefaultApplicationContext build(Properties config) {
        return build(config, newModuleFactory());
    }

    public DefaultApplicationContext build(Properties config, ModuleFactory moduleFactory) {
        Instrumentation instrumentation = new DummyInstrumentation();
        ObjectName objectName = new ObjectNameV1("mockAgentId", "mockAgentName", "mockApplicationName");

        AgentOption agentOption = new com.navercorp.pinpoint.profiler.AgentOption(instrumentation,
                config, Collections.emptyMap(), null,
                Collections.emptyList(), Collections.emptyList(), false);
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(config);
        AgentContextOption agentContextOption = AgentContextOptionBuilder.build(agentOption,
                objectName,
                profilerConfig);
        return new DefaultApplicationContext(agentContextOption, moduleFactory);
    }


    private ModuleFactory newModuleFactory() {
        Module pluginModule = new MockApplicationContextModule();
        return new OverrideModuleFactory(pluginModule);
    }
}
