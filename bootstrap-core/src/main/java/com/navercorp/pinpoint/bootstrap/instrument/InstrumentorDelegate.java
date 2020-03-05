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

package com.navercorp.pinpoint.bootstrap.instrument;


import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackChecker;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.common.util.Assert;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class InstrumentorDelegate implements Instrumentor {
    private final ProfilerConfig profilerConfig;
    private final InstrumentContext instrumentContext;

    public InstrumentorDelegate(ProfilerConfig profilerConfig, InstrumentContext instrumentContext) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig");
        }
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext");
        }
        this.profilerConfig = profilerConfig;
        this.instrumentContext = instrumentContext;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return instrumentContext.getInstrumentClass(classLoader, className, protectionDomain, classfileBuffer);
    }

    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classfileBuffer) {
        return instrumentContext.getInstrumentClass(classLoader, className, null, classfileBuffer);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className) {
        return instrumentContext.exist(classLoader, className, null);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain) {
        return instrumentContext.exist(classLoader, className, protectionDomain);
    }

    @Override
    public InterceptorScope getInterceptorScope(String scopeName) {
        return instrumentContext.getInterceptorScope(scopeName);
    }

    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        return instrumentContext.injectClass(targetClassLoader, className);
    }

    @Override
    public void transform(ClassLoader classLoader, String targetClassName, TransformCallback transformCallback) {
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallback);
    }

    @Override
    public void transform(ClassLoader classLoader, String targetClassName, Class<? extends TransformCallback> transformCallbackClass) {
        Assert.requireNonNull(transformCallbackClass, "transformCallback");
        TransformCallbackChecker.validate(transformCallbackClass);

        final String transformCallbackClassName = transformCallbackClass.getName();
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallbackClassName);
    }

    @Override
    public void retransform(Class<?> target, TransformCallback transformCallback) {
        instrumentContext.retransform(target, transformCallback);
    }

}
