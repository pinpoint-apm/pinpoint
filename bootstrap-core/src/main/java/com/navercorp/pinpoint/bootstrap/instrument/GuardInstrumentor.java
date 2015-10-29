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


import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;

/**
 * @author emeroad
 */
public class GuardInstrumentor implements Instrumentor {
    private final Instrumentor instrument;
    private boolean closed = false;

    public GuardInstrumentor(Instrumentor instrument) {
        if (instrument == null) {
            throw new NullPointerException("instrument must not be null");
        }
        this.instrument = instrument;
    }

    @Override
    public TraceContext getTraceContext() {
        checkOpen();
        return instrument.getTraceContext();
    }

    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classfileBuffer) {
        checkOpen();
        return instrument.getInstrumentClass(classLoader, className, classfileBuffer);
    }

    @Override
    public boolean exist(ClassLoader classLoader, String className) {
        checkOpen();
        return instrument.exist(classLoader, className);
    }

    @Override
    public InterceptorGroup getInterceptorGroup(String name) {
        checkOpen();
        return instrument.getInterceptorGroup(name);
    }

    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        checkOpen();
        return instrument.injectClass(targetClassLoader, className);
    }

    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName, TransformCallback transformCallback) {
        checkOpen();
        instrument.addClassFileTransformer(classLoader, targetClassName, transformCallback);
    }

    @Override
    public void retransform(Class<?> target, TransformCallback transformCallback) {
        checkOpen();
        instrument.retransform(target, transformCallback);
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
