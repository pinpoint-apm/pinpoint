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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;

public class AnnotatedInterceptorFactory implements InterceptorFactory {
    private final TraceContext traceContext;
    private final ProfilerPluginContext pluginContext;
    private final ByteCodeInstrumentor instrumentor;
    
    private final Class<? extends Interceptor> interceptorType;
    private final Object[] providedValues;
    
    
    public AnnotatedInterceptorFactory(TraceContext traceContext, ProfilerPluginContext pluginContext, ByteCodeInstrumentor instrumentor, Class<? extends Interceptor> interceptorType, Object[] providedArguments) {
        this.traceContext = traceContext;
        this.pluginContext = pluginContext;
        this.instrumentor = instrumentor;
        this.interceptorType = interceptorType;
        this.providedValues = providedArguments;
    }

    @Override
    public Interceptor getInterceptor(ClassLoader classLoader, InstrumentClass target, MethodInfo targetMethod) {
        AutoBindingObjectFactory<Interceptor> factory = new AutoBindingObjectFactory<Interceptor>(traceContext, pluginContext, instrumentor, target, targetMethod, providedValues);
        return factory.createInstance(interceptorType);
    }
}
