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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.module.ConfiguredApplicationType;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjectorFactory;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContextLoader;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginContextLoader;
import com.navercorp.pinpoint.profiler.plugin.config.PluginLoadingConfig;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ProfilerPluginContextLoaderProvider implements Provider<ProfilerPluginContextLoader> {

    private final ProfilerConfig profilerConfig;
    private final PluginLoadingConfig pluginLoadingConfig;
    private final ServiceType configuredApplicationType;
    private final PluginSetup pluginSetup;
    private final ClassInjectorFactory classInjectorFactory;
    private final List<PluginJar> pluginJars;

    @Inject
    public ProfilerPluginContextLoaderProvider(ProfilerConfig profilerConfig,
                                               PluginLoadingConfig pluginLoadingConfig,
                                               @ConfiguredApplicationType ServiceType configuredApplicationType,
                                               PluginSetup pluginSetup,
                                               InstrumentEngine instrumentEngine, BootstrapCore bootstrapCore,
                                               @PluginJars List<PluginJar> pluginJars) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.pluginLoadingConfig = Objects.requireNonNull(pluginLoadingConfig, "pluginLoadingConfig");

        this.configuredApplicationType = Objects.requireNonNull(configuredApplicationType, "configuredApplicationType");
        this.pluginSetup = Objects.requireNonNull(pluginSetup, "pluginSetup");
        Objects.requireNonNull(instrumentEngine, "instrumentEngine");
        Objects.requireNonNull(bootstrapCore, "bootstrapCore");
        this.classInjectorFactory = new ClassInjectorFactory(instrumentEngine, bootstrapCore);
        this.pluginJars = Objects.requireNonNull(pluginJars, "pluginJars");
    }

    @Override
    public ProfilerPluginContextLoader get() {
        return new DefaultProfilerPluginContextLoader(profilerConfig, pluginLoadingConfig, configuredApplicationType, classInjectorFactory, pluginSetup, pluginJars);
    }
}
