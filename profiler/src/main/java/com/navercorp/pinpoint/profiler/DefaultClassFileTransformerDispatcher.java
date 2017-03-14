/*
 * Copyright 2014 NAVER Corp.
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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.transformer.DebugTransformerRegistry;
import com.navercorp.pinpoint.profiler.instrument.transformer.TransformerRegistry;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.instrument.transformer.DefaultTransformerRegistry;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 */
public class DefaultClassFileTransformerDispatcher implements ClassFileTransformerDispatcher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ClassLoader agentClassLoader = this.getClass().getClassLoader();

    private final TransformerRegistry transformerRegistry;
    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    private final TransformerRegistry debugTransformerRegistry;

    private final ClassFileFilter pinpointClassFilter;
    private final ClassFileFilter unmodifiableFilter;

    @Inject
    public DefaultClassFileTransformerDispatcher(ProfilerConfig profilerConfig, PluginContextLoadResult pluginContextLoadResult, InstrumentEngine instrumentEngine,
                                                 DynamicTransformTrigger dynamicTransformTrigger, DynamicTransformerRegistry dynamicTransformerRegistry) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (pluginContextLoadResult == null) {
            throw new NullPointerException("pluginContexts must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (dynamicTransformerRegistry == null) {
            throw new NullPointerException("dynamicTransformerRegistry must not be null");
        }

        this.debugTransformerRegistry = new DebugTransformerRegistry(profilerConfig, instrumentEngine, dynamicTransformTrigger);

        this.pinpointClassFilter = new PinpointClassFilter(agentClassLoader);
        this.unmodifiableFilter = new UnmodifiableClassFilter();

        this.transformerRegistry = createTransformerRegistry(pluginContextLoadResult);
        this.dynamicTransformerRegistry = dynamicTransformerRegistry;
    }


    @Override
    public byte[] transform(ClassLoader classLoader, String classInternalName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (!pinpointClassFilter.accept(classLoader, classInternalName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        final ClassFileTransformer dynamicTransformer = dynamicTransformerRegistry.getTransformer(classLoader, classInternalName);
        if (dynamicTransformer != null) {
            return transform0(classLoader, classInternalName, classBeingRedefined, protectionDomain, classFileBuffer, dynamicTransformer);
        }

        if (!unmodifiableFilter.accept(classLoader, classInternalName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        ClassFileTransformer transformer = this.transformerRegistry.findTransformer(classInternalName);
        if (transformer == null) {
            // For debug
            // TODO What if a modifier is duplicated?
            transformer = this.debugTransformerRegistry.findTransformer(classInternalName);
            if (transformer == null) {
                return null;
            }
        }

        return transform0(classLoader, classInternalName, classBeingRedefined, protectionDomain, classFileBuffer, transformer);
    }

    private byte[] transform0(ClassLoader classLoader, String classInternalName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer, ClassFileTransformer transformer) {
        final String className = JavaAssistUtils.jvmNameToJavaName(classInternalName);

        if (isDebug) {
            if (classBeingRedefined == null) {
                logger.debug("[transform] classLoader:{} className:{} transformer:{}", classLoader, className, transformer.getClass().getName());
            } else {
                logger.debug("[retransform] classLoader:{} className:{} transformer:{}", classLoader, className, transformer.getClass().getName());
            }
        }

        try {
            final Thread thread = Thread.currentThread();
            final ClassLoader before = getContextClassLoader(thread);
            thread.setContextClassLoader(this.agentClassLoader);
            try {
                return transformer.transform(classLoader, className, classBeingRedefined, protectionDomain, classFileBuffer);
            } finally {
                // The context class loader have to be recovered even if it was null.
                thread.setContextClassLoader(before);
            }
        } catch (Throwable e) {
            logger.error("Transformer:{} threw an exception. cl:{} ctxCl:{} agentCl:{} Cause:{}",
                    transformer.getClass().getName(), classLoader, Thread.currentThread().getContextClassLoader(), agentClassLoader, e.getMessage(), e);
            return null;
        }
    }


    private ClassLoader getContextClassLoader(Thread thread) throws Throwable {
        try {
            return thread.getContextClassLoader();
        } catch (SecurityException se) {
            throw se;
        } catch (Throwable th) {
            if (isDebug) {
                logger.debug("getContextClassLoader(). Caused:{}", th.getMessage(), th);
            }
            throw th;
        }
    }

    private TransformerRegistry createTransformerRegistry(PluginContextLoadResult pluginContexts) {
        DefaultTransformerRegistry registry = new DefaultTransformerRegistry();

        for (ClassFileTransformer transformer : pluginContexts.getClassFileTransformer()) {
            if (transformer instanceof MatchableClassFileTransformer) {
                MatchableClassFileTransformer t = (MatchableClassFileTransformer) transformer;
                logger.info("Registering class file transformer {} for {} ", t, t.getMatcher());
                registry.addTransformer(t.getMatcher(), t);
            } else {
                logger.warn("Ignore class file transformer {}", transformer);
            }
        }

        return registry;
    }
}