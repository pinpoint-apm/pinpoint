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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

import java.io.InputStream;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class GuardInstrumentContext implements InstrumentContext {
    private final InstrumentContext instrumentContext;
    private boolean closed = false;

    public GuardInstrumentContext(InstrumentContext instrumentContext) {
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext");
        }

        this.instrumentContext = instrumentContext;
    }


    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        checkOpen();
        return instrumentContext.getInstrumentClass(classLoader, className, protectionDomain, classfileBuffer);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain) {
        checkOpen();
        return instrumentContext.exist(classLoader, className, protectionDomain);
    }

    @Override
    public InterceptorScope getInterceptorScope(String name) {
        checkOpen();
        return instrumentContext.getInterceptorScope(name);
    }

    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        checkOpen();
        return instrumentContext.injectClass(targetClassLoader, className);
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath) {
        checkOpen();
        return instrumentContext.getResourceAsStream(targetClassLoader, classPath);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName, TransformCallback transformCallback) {
        checkOpen();
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallback);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName, String transformCallbackClass) {
        checkOpen();
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallbackClass);
    }

    @Override
    public void addClassFileTransformer(Matcher matcher, TransformCallback transformCallback) {
        checkOpen();
        instrumentContext.addClassFileTransformer(matcher, transformCallback);
    }

    @Override
    public void addClassFileTransformer(Matcher matcher, String transformCallbackClassName) {
        checkOpen();
        instrumentContext.addClassFileTransformer(matcher, transformCallbackClassName);
    }

    @Override
    public void addClassFileTransformer(Matcher matcher, String transformCallbackClassName, Object[] parameters, Class<?>[] parameterType) {
        checkOpen();
        instrumentContext.addClassFileTransformer(matcher, transformCallbackClassName, parameters, parameterType);
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
            throw new IllegalStateException("instrumentContext already closed");
        }
    }
}
