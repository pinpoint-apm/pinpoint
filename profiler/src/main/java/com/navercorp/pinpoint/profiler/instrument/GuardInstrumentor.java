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

package com.navercorp.pinpoint.profiler.instrument;


import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackChecker;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.common.util.Assert;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class GuardInstrumentor implements Instrumentor {
    private final ProfilerConfig profilerConfig;
    private final InstrumentContext instrumentContext;
    private boolean closed = false;

    public GuardInstrumentor(ProfilerConfig profilerConfig, InstrumentContext instrumentContext) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.instrumentContext = Assert.requireNonNull(instrumentContext, "instrumentContext");
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        checkOpen();
        return instrumentContext.getInstrumentClass(classLoader, className, protectionDomain, classfileBuffer);
    }

    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classfileBuffer) {
        checkOpen();
        return instrumentContext.getInstrumentClass(classLoader, className, null, classfileBuffer);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain) {
        checkOpen();
        return instrumentContext.exist(classLoader, className, protectionDomain);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className) {
        checkOpen();
        return instrumentContext.exist(classLoader, className, null);
    }


    @Override
    public InterceptorScope getInterceptorScope(String scopeName) {
        checkOpen();
        return instrumentContext.getInterceptorScope(scopeName);
    }

    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        checkOpen();
        return instrumentContext.injectClass(targetClassLoader, className);
    }

    @Override
    public void transform(ClassLoader classLoader, String targetClassName, TransformCallback transformCallback) {
        checkOpen();
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallback);
    }

    @Override
    public void transform(ClassLoader classLoader, String targetClassName, Class<? extends TransformCallback> transformCallback) {
        checkOpen();
        Assert.requireNonNull(transformCallback, "transformCallback");
        TransformCallbackChecker.validate(transformCallback);

        final String transformCallbackClassName = transformCallback.getName();
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallbackClassName);
    }

    @Override
    public void retransform(Class<?> target, TransformCallback transformCallback) {
        checkOpen();
        instrumentContext.retransform(target, transformCallback);
    }

    public void close() {
        this.closed = true;
    }

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("Instrumentor already closed");
        }
    }
}
