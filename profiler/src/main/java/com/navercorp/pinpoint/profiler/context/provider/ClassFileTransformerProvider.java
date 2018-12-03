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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.DefaultClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.DynamicTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.BaseTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.BypassLambdaClassFileResolver;
import com.navercorp.pinpoint.profiler.instrument.transformer.DebugTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.DefaultLambdaClassFileResolver;
import com.navercorp.pinpoint.profiler.instrument.transformer.DefaultTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.LambdaClassFileResolver;
import com.navercorp.pinpoint.profiler.instrument.transformer.MatchableTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.TransformerRegistry;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassFileTransformerProvider implements Provider<ClassFileTransformer> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;
    private final PluginContextLoadResult pluginContextLoadResult;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    @Inject
    public ClassFileTransformerProvider(ProfilerConfig profilerConfig, InstrumentEngine instrumentEngine, PluginContextLoadResult pluginContextLoadResult,
                                        DynamicTransformTrigger dynamicTransformTrigger, DynamicTransformerRegistry dynamicTransformerRegistry) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.instrumentEngine = Assert.requireNonNull(instrumentEngine, "instrumentEngine must not be null");
        this.pluginContextLoadResult = Assert.requireNonNull(pluginContextLoadResult, "pluginContextLoadResult must not be null");
        this.dynamicTransformTrigger = Assert.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger must not be null");
        this.dynamicTransformerRegistry = Assert.requireNonNull(dynamicTransformerRegistry, "dynamicTransformerRegistry must not be null");
    }

    @Override
    public ClassFileTransformer get() {

        final LambdaClassFileResolver lambdaClassFileResolver = newLambdaClassFileResolver(profilerConfig);

        final BaseTransformerRegistry baseTransformerRegistry = newDefaultTransformerRegistry();
        final TransformerRegistry transformerRegistry = setupTransformerRegistry(baseTransformerRegistry, pluginContextLoadResult);
        final TransformerRegistry debugTransformerRegistry = new DebugTransformerRegistry(profilerConfig, instrumentEngine, dynamicTransformTrigger);
        return new DefaultClassFileTransformerDispatcher(transformerRegistry, debugTransformerRegistry, dynamicTransformerRegistry, lambdaClassFileResolver);
    }


    private LambdaClassFileResolver newLambdaClassFileResolver(ProfilerConfig profilerConfig) {
        if (profilerConfig.isSupportLambdaExpressions()) {
            return new DefaultLambdaClassFileResolver();
        }
        return new BypassLambdaClassFileResolver();
    }

    private BaseTransformerRegistry newDefaultTransformerRegistry() {
        if (this.profilerConfig.isInstrumentMatcherEnable()) {
            return new MatchableTransformerRegistry(profilerConfig);
        }
        return new DefaultTransformerRegistry();
    }

    private TransformerRegistry setupTransformerRegistry(BaseTransformerRegistry registry, PluginContextLoadResult pluginContexts) {
        Assert.requireNonNull(registry, "registry must not be null");
        Assert.requireNonNull(pluginContexts, "pluginContexts must not be null");

        for (ClassFileTransformer transformer : pluginContexts.getClassFileTransformer()) {
            if (transformer instanceof MatchableClassFileTransformer) {
                MatchableClassFileTransformer t = (MatchableClassFileTransformer) transformer;
                if (logger.isInfoEnabled()) {
                    logger.info("Registering class file transformer {} for {} ", t, t.getMatcher());
                }
                try {
                    registry.addTransformer(t.getMatcher(), t);
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to add transformer {}", transformer, e);
                    }
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Ignore class file transformer {}", transformer);
                }
            }
        }

        return registry;
    }

}