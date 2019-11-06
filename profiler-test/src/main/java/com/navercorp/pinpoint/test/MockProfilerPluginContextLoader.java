/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginGlobalContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginGlobalContext;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.PluginSetupResult;
import com.navercorp.pinpoint.profiler.plugin.PluginsSetupResult;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginContextLoader;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class MockProfilerPluginContextLoader implements ProfilerPluginContextLoader {

    private final ProfilerConfig profilerConfig;
    private final ServiceType configuredApplicationType;
    private final PluginSetup pluginSetup;
    private final ClassInjector classInjector = new TestProfilerPluginClassInjector();

    public MockProfilerPluginContextLoader(ProfilerConfig profilerConfig, ServiceType configuredApplicationType,
                                           PluginSetup pluginSetup) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.configuredApplicationType = Assert.requireNonNull(configuredApplicationType, "configuredApplicationType");
        this.pluginSetup = Assert.requireNonNull(pluginSetup, "pluginSetup");
    }

    @Override
    public PluginsSetupResult load(List<ProfilerPlugin> profilerPlugins) {
        ProfilerPluginGlobalContext globalContext = new DefaultProfilerPluginGlobalContext(profilerConfig, configuredApplicationType);
        PluginsSetupResult pluginsSetupResult = new PluginsSetupResult();
        for (ProfilerPlugin profilerPlugin : profilerPlugins) {
            PluginSetupResult context = pluginSetup.setupPlugin(globalContext, profilerPlugin, classInjector);
            pluginsSetupResult.addPluginSetupResult(context);
        }
        ServiceType detectedApplicationType = globalContext.getApplicationType();
        pluginsSetupResult.setApplicationType(detectedApplicationType);

        return pluginsSetupResult;
    }
}
