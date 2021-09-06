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
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import java.util.Objects;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.classloading.DebugTransformerClassInjector;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import com.navercorp.pinpoint.profiler.instrument.transformer.BypassLambdaClassFileResolver;
import com.navercorp.pinpoint.profiler.instrument.transformer.DebugTransformer;
import com.navercorp.pinpoint.profiler.instrument.transformer.DebugTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.DefaultLambdaClassFileResolver;
import com.navercorp.pinpoint.profiler.instrument.transformer.DefaultTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.LambdaClassFileResolver;
import com.navercorp.pinpoint.profiler.instrument.transformer.MatchableTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.TransformerRegistry;
import com.navercorp.pinpoint.profiler.plugin.ClassFileTransformerLoader;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginInstrumentContext;
import com.navercorp.pinpoint.profiler.transformer.ClassFileFilter;
import com.navercorp.pinpoint.profiler.transformer.DefaultClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.transformer.DelegateTransformerRegistry;
import com.navercorp.pinpoint.profiler.transformer.DynamicTransformerRegistry;
import com.navercorp.pinpoint.profiler.transformer.PinpointClassFilter;
import com.navercorp.pinpoint.profiler.transformer.UnmodifiableClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassFileTransformerProvider implements Provider<ClassFileTransformer> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;
    private final InstrumentConfig instrumentConfig;
    private final InstrumentMatcherCacheConfig instrumentMatcherCacheConfig;
    private final PluginContextLoadResult pluginContextLoadResult;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    @Inject
    public ClassFileTransformerProvider(ProfilerConfig profilerConfig,
                                        InstrumentConfig instrumentConfig,
                                        InstrumentMatcherCacheConfig instrumentMatcherCacheConfig,
                                        InstrumentEngine instrumentEngine, PluginContextLoadResult pluginContextLoadResult,
                                        DynamicTransformTrigger dynamicTransformTrigger, DynamicTransformerRegistry dynamicTransformerRegistry) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.instrumentConfig = Objects.requireNonNull(instrumentConfig, "instrumentConfig");
        this.instrumentMatcherCacheConfig = Objects.requireNonNull(instrumentMatcherCacheConfig, "instrumentMatcherCacheConfig");
        this.instrumentEngine = Objects.requireNonNull(instrumentEngine, "instrumentEngine");

        this.pluginContextLoadResult = Objects.requireNonNull(pluginContextLoadResult, "pluginContextLoadResult");
        this.dynamicTransformTrigger = Objects.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger");
        this.dynamicTransformerRegistry = Objects.requireNonNull(dynamicTransformerRegistry, "dynamicTransformerRegistry");
    }

    @Override
    public ClassFileTransformer get() {

        final LambdaClassFileResolver lambdaClassFileResolver = newLambdaClassFileResolver(instrumentConfig.isSupportLambdaExpressions());

        final TransformerRegistry transformerRegistry = newTransformerRegistry();

        final String classFilterBasePackage = StringUtils.defaultString(instrumentConfig.getPinpointBasePackage(), PinpointClassFilter.DEFAULT_PACKAGE);
        List<String> excludeSub = StringUtils.tokenizeToStringList(instrumentConfig.getPinpointExcludeSubPackage(), ",");
        if (excludeSub.isEmpty()) {
            excludeSub = PinpointClassFilter.DEFAULT_EXCLUDES;
        }

        final ClassFileFilter pinpointClassFilter = new PinpointClassFilter(classFilterBasePackage, excludeSub);

        final List<String> allowJdkClassName = instrumentConfig.getAllowJdkClassName();
        final ClassFileFilter unmodifiableFilter = new UnmodifiableClassFilter(allowJdkClassName);
        return new DefaultClassFileTransformerDispatcher(pinpointClassFilter, unmodifiableFilter, transformerRegistry,
                dynamicTransformerRegistry, lambdaClassFileResolver);
    }

    private TransformerRegistry newTransformerRegistry() {
        final List<MatchableClassFileTransformer> matchableClassFileTransformerList = getMatchableTransformers(pluginContextLoadResult);
        final TransformerRegistry transformerRegistry = newDefaultTransformerRegistry(matchableClassFileTransformerList);

        final TransformerRegistry debugTransformerRegistry = newDebugTransformerRegistry();

        return new DelegateTransformerRegistry(transformerRegistry, debugTransformerRegistry);
    }

    private TransformerRegistry newDebugTransformerRegistry() {
        final DebugTransformer debugTransformer = newDebugTransformer(profilerConfig, instrumentEngine, dynamicTransformTrigger);
        final Filter<String> debugTargetFilter = instrumentConfig.getProfilableClassFilter();
        return new DebugTransformerRegistry(debugTargetFilter, debugTransformer);
    }

    private DebugTransformer newDebugTransformer(ProfilerConfig profilerConfig, InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger) {

        ClassInjector classInjector = new DebugTransformerClassInjector();

        ClassFileTransformerLoader transformerRegistry = new ClassFileTransformerLoader(profilerConfig, dynamicTransformTrigger);
        InstrumentContext debugContext = new PluginInstrumentContext(profilerConfig, instrumentEngine, dynamicTransformTrigger, classInjector, transformerRegistry);

        return new DebugTransformer(instrumentEngine, debugContext);
    }


    private LambdaClassFileResolver newLambdaClassFileResolver(boolean isSupportLambdaExpressions) {
        if (isSupportLambdaExpressions) {
            return new DefaultLambdaClassFileResolver();
        }
        return new BypassLambdaClassFileResolver();
    }

    private TransformerRegistry newDefaultTransformerRegistry(List<MatchableClassFileTransformer> matchableClassFileTransformerList) {
        if (this.instrumentMatcherCacheConfig.isInstrumentMatcherEnable()) {
            return new MatchableTransformerRegistry(this.instrumentMatcherCacheConfig, matchableClassFileTransformerList);
        }
        return new DefaultTransformerRegistry(matchableClassFileTransformerList);
    }

    private List<MatchableClassFileTransformer> getMatchableTransformers(PluginContextLoadResult pluginContexts) {
        Objects.requireNonNull(pluginContexts, "pluginContexts");

        final List<MatchableClassFileTransformer> matcherList = new ArrayList<>();
        for (ClassFileTransformer transformer : pluginContexts.getClassFileTransformer()) {
            if (transformer instanceof MatchableClassFileTransformer) {
                final MatchableClassFileTransformer t = (MatchableClassFileTransformer) transformer;
                if (logger.isInfoEnabled()) {
                    logger.info("Registering class file transformer {} for {} ", t, t.getMatcher());
                }
                matcherList.add(t);
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Ignore class file transformer {}", transformer);
                }
            }
        }

        return matcherList;
    }

}