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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.ConfiguredApplicationType;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginContextLoadResultProvider implements Provider<PluginContextLoadResult> {

    private final ProfilerConfig profilerConfig;
    private final ServiceType configuredApplicationType;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final PluginLoader pluginLoader;

    @Inject
    public MockPluginContextLoadResultProvider(ProfilerConfig profilerConfig, @ConfiguredApplicationType ServiceType configuredApplicationType,
                                               InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger, PluginLoader pluginLoader) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.configuredApplicationType = Assert.requireNonNull(configuredApplicationType, "configuredApplicationType must not be null");
        this.instrumentEngine = Assert.requireNonNull(instrumentEngine, "instrumentEngine must not be null");
        this.dynamicTransformTrigger = Assert.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger must not be null");
        this.pluginLoader = Assert.requireNonNull(pluginLoader, "pluginLoader must not be null");
    }

    @Override
    public PluginContextLoadResult get() {
        return new MockPluginContextLoadResult(profilerConfig, configuredApplicationType, instrumentEngine, dynamicTransformTrigger, pluginLoader);
    }


}
