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

package com.navercorp.pinpoint.profiler.objectfactory;

import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.interceptor.factory.AnnotatedInterceptorFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ObjectBinderFactory {
    private final ProfilerConfig profilerConfig;
    private final Provider<TraceContext> traceContext;

    public ObjectBinderFactory(ProfilerConfig profilerConfig, Provider<TraceContext> traceContext) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.traceContext = traceContext;
    }

    public AutoBindingObjectFactory newAutoBindingObjectFactory(InstrumentContext pluginContext, ClassLoader classLoader, ArgumentProvider... argumentProviders) {
        final TraceContext traceContext = this.traceContext.get();
        return new AutoBindingObjectFactory(profilerConfig, traceContext, pluginContext, classLoader, argumentProviders);
    }


    public InterceptorArgumentProvider newInterceptorArgumentProvider(InstrumentClass instrumentClass) {
        final TraceContext traceContext = this.traceContext.get();
        return new InterceptorArgumentProvider(traceContext, instrumentClass);
    }

    public AnnotatedInterceptorFactory newAnnotatedInterceptorFactory(InstrumentContext pluginContext, boolean exceptionHandle) {
        final TraceContext traceContext = this.traceContext.get();
        return new AnnotatedInterceptorFactory(profilerConfig, traceContext, pluginContext, exceptionHandle);
    }
}
