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

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.profiler.AgentContextOption;
import com.navercorp.pinpoint.profiler.AgentContextOptionBuilder;
import com.navercorp.pinpoint.profiler.AgentOption;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 * @author jaehong.kim
 */
public class PluginTestAgent extends DefaultAgent {

    public PluginTestAgent(Map<String, Object> agentOption) {
        super(agentOption);
    }

    @Override
    protected ApplicationContext newApplicationContext(AgentContextOption agentOption) {


        PluginApplicationContextModule pluginApplicationContextModule = new PluginApplicationContextModule();
        ModuleFactory moduleFactory = new OverrideModuleFactory(pluginApplicationContextModule);
        DefaultApplicationContext applicationContext = new DefaultApplicationContext(agentOption, moduleFactory);

        exportVerifier(applicationContext);

        return applicationContext ;

    }

    protected Path getLogConfigPath(ProfilerConfig config, Path agentPath) {
        if (agentPath == null) {
            URL classPathRoot = getClass().getResource("/");
            try {
                Objects.requireNonNull(classPathRoot, "resource");
                return Paths.get(classPathRoot.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to get agentPath", e);
            }
        }
        return agentPath;
    }

    @Override
    protected AgentContextOption buildContextOption(AgentOption agentOption, ProfilerConfig profilerConfig) {
        Properties properties = profilerConfig.getProperties();
        properties.put(DefaultProfilerConfig.PROFILER_INTERCEPTOR_EXCEPTION_PROPAGATE, "true");

        String agentId = "mockAgentId";
        String agentName = "mockAgentName";
        String applicationName = "mockApplicationName";
        return AgentContextOptionBuilder.build(agentOption,
                agentId, agentName, applicationName,
                profilerConfig);
    }

    private void exportVerifier(DefaultApplicationContext applicationContext) {
        PluginVerifierExternalAdaptor adaptor = new PluginVerifierExternalAdaptor(applicationContext);
        PluginTestVerifierHolder.setInstance(adaptor);
    }


}
