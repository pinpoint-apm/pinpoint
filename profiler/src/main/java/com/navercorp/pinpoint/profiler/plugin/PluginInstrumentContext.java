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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.profiler.context.scope.ConcurrentPool;
import com.navercorp.pinpoint.profiler.context.scope.InterceptorScopeFactory;
import com.navercorp.pinpoint.profiler.context.scope.Pool;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.scanner.ClassScannerFactory;
import com.navercorp.pinpoint.profiler.instrument.scanner.Scanner;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginInstrumentContext implements InstrumentContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProfilerConfig profilerConfig;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final ClassInjector classInjector;

    private final Pool<String, InterceptorScope> interceptorScopePool = new ConcurrentPool<String, InterceptorScope>(new InterceptorScopeFactory());

    private final ClassFileTransformerLoader transformerRegistry;

    public PluginInstrumentContext(ProfilerConfig profilerConfig, InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger, ClassInjector classInjector, ClassFileTransformerLoader transformerRegistry) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.instrumentEngine = Assert.requireNonNull(instrumentEngine, "instrumentEngine");
        this.dynamicTransformTrigger = Assert.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger");
        this.classInjector = Assert.requireNonNull(classInjector, "classInjector");
        this.transformerRegistry = Assert.requireNonNull(transformerRegistry, "transformerRegistry");
    }



    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (className == null) {
            throw new NullPointerException("className");
        }
        try {
            final InstrumentEngine instrumentEngine = getInstrumentEngine();
            return instrumentEngine.getClass(this, classLoader, className, protectionDomain, classFileBuffer);
        } catch (NotFoundInstrumentException e) {
            return null;
        }
    }


    @Override
    public boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain) {
        if (className == null) {
            throw new NullPointerException("className");
        }

        final String jvmClassName = JavaAssistUtils.javaClassNameToJvmResourceName(className);

        final Scanner scanner = ClassScannerFactory.newScanner(protectionDomain, classLoader);
        if (logger.isDebugEnabled()) {
            logger.debug("scanner:{}", scanner);
        }
        try {
            return scanner.exist(jvmClassName);
        } finally {
            scanner.close();
        }
    }

    private InstrumentEngine getInstrumentEngine() {
        return this.instrumentEngine;
    }

    @Override
    public void addClassFileTransformer(final Matcher matcher, final TransformCallback transformCallback) {
        Assert.requireNonNull(matcher, "matcher");
        Assert.requireNonNull(transformCallback, "transformCallback");
        final TransformCallbackProvider transformCallbackProvider = new InstanceTransformCallbackProvider(transformCallback);
        transformerRegistry.addClassFileTransformer(this, matcher, transformCallbackProvider);
    }

    @Override
    public void addClassFileTransformer(final Matcher matcher, final String transformCallbackClassName) {
        Assert.requireNonNull(matcher, "matcher");
        Assert.requireNonNull(transformCallbackClassName, "transformCallbackClassName");
        final TransformCallbackProvider transformCallbackProvider = new DynamicTransformCallbackProvider(transformCallbackClassName);
        transformerRegistry.addClassFileTransformer(this, matcher, transformCallbackProvider);
    }

    @Override
    public void addClassFileTransformer(final Matcher matcher, final String transformCallbackClassName, Object[] parameters, Class<?>[] parameterTypes) {
        Assert.requireNonNull(matcher, "matcher");
        Assert.requireNonNull(transformCallbackClassName, "transformCallbackClassName");
        final TransformCallbackProvider transformCallbackProvider = new DynamicTransformCallbackProvider(transformCallbackClassName, parameters, parameterTypes);
        transformerRegistry.addClassFileTransformer(this, matcher, transformCallbackProvider);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName, final TransformCallback transformCallback) {
        Assert.requireNonNull(targetClassName, "targetClassName");
        Assert.requireNonNull(transformCallback, "transformCallback");
        final TransformCallbackProvider transformCallbackProvider = new InstanceTransformCallbackProvider(transformCallback);
        this.transformerRegistry.addClassFileTransformer(this, classLoader, targetClassName, transformCallbackProvider);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName, final String transformCallbackClassName) {
        Assert.requireNonNull(targetClassName, "targetClassName");
        Assert.requireNonNull(transformCallbackClassName, "transformCallbackClassName");
        final TransformCallbackProvider transformCallbackProvider = new DynamicTransformCallbackProvider(transformCallbackClassName);
        this.transformerRegistry.addClassFileTransformer(this, classLoader, targetClassName, transformCallbackProvider);
    }


    @Override
    public void retransform(Class<?> target, final TransformCallback transformCallback) {
        Assert.requireNonNull(target, "target");
        Assert.requireNonNull(transformCallback, "transformCallback");

        final InstanceTransformCallbackProvider transformCallbackProvider = new InstanceTransformCallbackProvider(transformCallback);
        final ClassFileTransformerDelegate classFileTransformerGuardDelegate = new ClassFileTransformerDelegate(profilerConfig, this, transformCallbackProvider);

        this.dynamicTransformTrigger.retransform(target, classFileTransformerGuardDelegate);
    }


    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        if (className == null) {
            throw new NullPointerException("className");
        }

        return classInjector.injectClass(targetClassLoader, className);
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath) {
        if (classPath == null) {
            return null;
        }

        return classInjector.getResourceAsStream(targetClassLoader, classPath);
    }


    @Override
    public InterceptorScope getInterceptorScope(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        return interceptorScopePool.get(name);
    }
}
