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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import com.navercorp.pinpoint.profiler.plugin.DefaultPluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;

import java.net.URL;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginContextLoadResultProvider implements Provider<PluginContextLoadResult> {

    private final ProfilerConfig profilerConfig;
    private final InstrumentEngine instrumentEngine;
    private final URL[] pluginJars;
    private final DynamicTransformTrigger dynamicTransformTrigger;

    @Inject
    public PluginContextLoadResultProvider(ProfilerConfig profilerConfig, DynamicTransformTrigger dynamicTransformTrigger, InstrumentEngine instrumentEngine,
                                           @PluginJars URL[] pluginJars) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (pluginJars == null) {
            throw new NullPointerException("pluginJars must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.dynamicTransformTrigger = dynamicTransformTrigger;

        this.instrumentEngine = instrumentEngine;
        this.pluginJars = pluginJars;
    }

    @Override
    public PluginContextLoadResult get() {
        return new DefaultPluginContextLoadResult(profilerConfig, dynamicTransformTrigger, instrumentEngine, pluginJars);

    }
}
