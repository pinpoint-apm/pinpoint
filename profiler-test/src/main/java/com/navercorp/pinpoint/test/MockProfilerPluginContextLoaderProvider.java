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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.module.ConfiguredApplicationType;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginContextLoader;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class MockProfilerPluginContextLoaderProvider implements Provider<ProfilerPluginContextLoader> {

    private final ProfilerConfig profilerConfig;
    private final ServiceType configuredApplicationType;
    private final PluginSetup pluginSetup;

    @Inject
    public MockProfilerPluginContextLoaderProvider(ProfilerConfig profilerConfig,
                                                   @ConfiguredApplicationType ServiceType configuredApplicationType,
                                                   PluginSetup pluginSetup) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.configuredApplicationType = Objects.requireNonNull(configuredApplicationType, "configuredApplicationType");
        this.pluginSetup = Objects.requireNonNull(pluginSetup, "pluginSetup");
    }

    @Override
    public ProfilerPluginContextLoader get() {
        return new MockProfilerPluginContextLoader(profilerConfig, configuredApplicationType, pluginSetup);
    }
}
