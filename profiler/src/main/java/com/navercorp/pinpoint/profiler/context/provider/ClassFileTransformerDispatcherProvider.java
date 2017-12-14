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
import com.navercorp.pinpoint.profiler.MatchableClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.DefaultClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.DynamicTransformerRegistry;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassFileTransformerDispatcherProvider implements Provider<ClassFileTransformerDispatcher> {

    private final ProfilerConfig profilerConfig;
    private final PluginContextLoadResult pluginContextLoadResult;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    @Inject
    public ClassFileTransformerDispatcherProvider(ProfilerConfig profilerConfig, InstrumentEngine instrumentEngine, PluginContextLoadResult pluginContextLoadResult,
                                                  DynamicTransformTrigger dynamicTransformTrigger, DynamicTransformerRegistry dynamicTransformerRegistry) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (pluginContextLoadResult == null) {
            throw new NullPointerException("pluginContextLoadResult must not be null");
        }
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger must not be null");
        }
        if (dynamicTransformerRegistry == null) {
            throw new NullPointerException("dynamicTransformerRegistry must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.instrumentEngine = instrumentEngine;
        this.pluginContextLoadResult = pluginContextLoadResult;
        this.dynamicTransformTrigger = dynamicTransformTrigger;
        this.dynamicTransformerRegistry = dynamicTransformerRegistry;
    }

    @Override
    public ClassFileTransformerDispatcher get() {
        if(this.profilerConfig.isInstrumentMatcherEnable()) {
            return new MatchableClassFileTransformerDispatcher(profilerConfig, pluginContextLoadResult, instrumentEngine, dynamicTransformTrigger, dynamicTransformerRegistry);
        }
        return new DefaultClassFileTransformerDispatcher(profilerConfig, pluginContextLoadResult, instrumentEngine, dynamicTransformTrigger, dynamicTransformerRegistry);
    }
}