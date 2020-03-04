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

package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.ConfiguredApplicationType;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjectorFactory;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContextLoader;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginContextLoader;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ProfilerPluginContextLoaderProvider implements Provider<ProfilerPluginContextLoader> {

    private final ProfilerConfig profilerConfig;
    private final ServiceType configuredApplicationType;
    private final PluginSetup pluginSetup;
    private final ClassInjectorFactory classInjectorFactory;
    private final List<PluginJar> pluginJars;

    @Inject
    public ProfilerPluginContextLoaderProvider(ProfilerConfig profilerConfig,
                                               @ConfiguredApplicationType ServiceType configuredApplicationType,
                                               PluginSetup pluginSetup,
                                               InstrumentEngine instrumentEngine, BootstrapCore bootstrapCore,
                                               @PluginJars List<PluginJar> pluginJars) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.configuredApplicationType = Assert.requireNonNull(configuredApplicationType, "configuredApplicationType");
        this.pluginSetup = Assert.requireNonNull(pluginSetup, "pluginSetup");
        Assert.requireNonNull(instrumentEngine, "instrumentEngine");
        Assert.requireNonNull(bootstrapCore, "bootstrapCore");
        this.classInjectorFactory = new ClassInjectorFactory(instrumentEngine, bootstrapCore);
        this.pluginJars = Assert.requireNonNull(pluginJars, "pluginJars");
    }

    @Override
    public ProfilerPluginContextLoader get() {
        return new DefaultProfilerPluginContextLoader(profilerConfig, configuredApplicationType, classInjectorFactory, pluginSetup, pluginJars);
    }
}
