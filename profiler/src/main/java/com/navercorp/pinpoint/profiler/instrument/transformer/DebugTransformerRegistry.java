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

package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.classloading.DebugTransformerClassInjector;
import com.navercorp.pinpoint.profiler.instrument.classloading.LegacyProfilerPluginClassInjector;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.plugin.ClassFileTransformerLoader;
import com.navercorp.pinpoint.profiler.plugin.PluginInstrumentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DebugTransformerRegistry implements TransformerRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // TODO remove next release
    private static final String DEBUG_INJECTOR_TYPE = "profiler.debug.injector.type";
    private static final String LEGACY = "legacy";
    private static final String DEBUG = "debug";

    private final Filter<String> debugTargetFilter;
    private final DebugTransformer debugTransformer;

    public DebugTransformerRegistry(ProfilerConfig profilerConfig, InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger must not be null");
        }
        this.debugTargetFilter = profilerConfig.getProfilableClassFilter();
        this.debugTransformer = newDebugTransformer(profilerConfig, instrumentEngine, dynamicTransformTrigger);
    }

    private DebugTransformer newDebugTransformer(ProfilerConfig profilerConfig, InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger) {

        ClassInjector classInjector = newClassInjector(profilerConfig);

        ClassFileTransformerLoader transformerRegistry = new ClassFileTransformerLoader(profilerConfig, dynamicTransformTrigger);
        InstrumentContext debugContext = new PluginInstrumentContext(profilerConfig, instrumentEngine, dynamicTransformTrigger, classInjector, transformerRegistry);

        return new DebugTransformer(instrumentEngine, debugContext);
    }

    private ClassInjector newClassInjector(ProfilerConfig profilerConfig) {
        // TODO remove next release
        //  bug workaround for DebugTransformerClassInjector
        final String debugInjectorType = profilerConfig.readString(DEBUG_INJECTOR_TYPE, DEBUG);
        logger.info("{}:{}", DEBUG_INJECTOR_TYPE, debugInjectorType);
        if (LEGACY.equals(debugInjectorType)) {
            return new LegacyProfilerPluginClassInjector(getClass().getClassLoader());
        }
        logger.info("newDebugTransformerClassInjector()");
        return new DebugTransformerClassInjector();

    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer) {
        return findTransformer(classLoader, classInternalName, classFileBuffer, null);
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer, InternalClassMetadata classMetadata) {
        if (classInternalName == null) {
            throw new NullPointerException("classInternalName must not be null");
        }
        if (this.debugTargetFilter.filter(classInternalName)) {
            // Added to see if call stack view is OK on a test machine.
            return debugTransformer;
        }
        return null;
    }
}