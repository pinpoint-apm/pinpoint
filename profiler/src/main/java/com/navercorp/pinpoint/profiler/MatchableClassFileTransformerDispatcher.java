/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadataReader;
import com.navercorp.pinpoint.profiler.instrument.transformer.DebugTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.MatchableTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.TransformerRegistry;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class MatchableClassFileTransformerDispatcher implements ClassFileTransformerDispatcher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ClassLoader agentClassLoader = this.getClass().getClassLoader();

    private final BaseClassFileTransformer baseClassFileTransformer;
    private final TransformerRegistry transformerRegistry;
    private final DynamicTransformerRegistry dynamicTransformerRegistry;
    private final TransformerRegistry debugTransformerRegistry;

    private final ClassFileFilter classLoaderFilter;
    private final ClassFileFilter pinpointClassFilter;
    private final ClassFileFilter unmodifiableFilter;

    private final boolean supportLambdaExpressions;

    public MatchableClassFileTransformerDispatcher(ProfilerConfig profilerConfig, PluginContextLoadResult pluginContextLoadResult, InstrumentEngine instrumentEngine,
                                                   DynamicTransformTrigger dynamicTransformTrigger, DynamicTransformerRegistry dynamicTransformerRegistry) {
        Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        Assert.requireNonNull(pluginContextLoadResult, "pluginContexts must not be null");
        Assert.requireNonNull(instrumentEngine, "instrumentEngine must not be null");
        Assert.requireNonNull(dynamicTransformerRegistry, "dynamicTransformerRegistry must not be null");

        this.baseClassFileTransformer = new BaseClassFileTransformer(agentClassLoader);
        this.debugTransformerRegistry = new DebugTransformerRegistry(profilerConfig, instrumentEngine, dynamicTransformTrigger);
        this.transformerRegistry = createTransformerRegistry(pluginContextLoadResult, profilerConfig);
        this.dynamicTransformerRegistry = dynamicTransformerRegistry;

        this.classLoaderFilter = new PinpointClassLoaderFilter(agentClassLoader);
        this.pinpointClassFilter = new PinpointClassFilter();
        this.unmodifiableFilter = new UnmodifiableClassFilter();

        this.supportLambdaExpressions = profilerConfig.isSupportLambdaExpressions();
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String classInternalName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (!classLoaderFilter.accept(classLoader, classInternalName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        InternalClassMetadata classMetadata = null;
        String internalName = classInternalName;
        if (internalName == null) {
            if (this.supportLambdaExpressions) {
                // proxy-like class specific for lambda expressions.
                // e.g. Example$$Lambda$1/1072591677
                try {
                    classMetadata = InternalClassMetadataReader.readInternalClassMetadata(classFileBuffer);
                    internalName = classMetadata.getClassInternalName();
                } catch (Exception e) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Failed to read metadata of lambda expressions. classLoader={}", classLoader, e);
                    }
                    return null;
                }
            } else {
                // unsupported lambda expressions.
                return null;
            }
        }

        if (internalName == null) {
            return null;
        }

        if (!pinpointClassFilter.accept(classLoader, internalName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        final ClassFileTransformer dynamicTransformer = dynamicTransformerRegistry.getTransformer(classLoader, internalName);
        if (dynamicTransformer != null) {
            return baseClassFileTransformer.transform(classLoader, internalName, classBeingRedefined, protectionDomain, classFileBuffer, dynamicTransformer);
        }

        if (!unmodifiableFilter.accept(classLoader, internalName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        ClassFileTransformer transformer = this.transformerRegistry.findTransformer(classLoader, internalName, classFileBuffer, classMetadata);
        if (transformer == null) {
            // For debug
            // TODO What if a modifier is duplicated?
            transformer = this.debugTransformerRegistry.findTransformer(classLoader, internalName, classFileBuffer);
            if (transformer == null) {
                return null;
            }
        }

        return baseClassFileTransformer.transform(classLoader, internalName, classBeingRedefined, protectionDomain, classFileBuffer, transformer);
    }

    private TransformerRegistry createTransformerRegistry(PluginContextLoadResult pluginContexts, final ProfilerConfig profilerConfig) {
        final MatchableTransformerRegistry registry = new MatchableTransformerRegistry(profilerConfig);
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