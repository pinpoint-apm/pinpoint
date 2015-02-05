/**
 * Copyright 2014 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.plugin.Scope;
import com.navercorp.pinpoint.bootstrap.plugin.Singleton;
import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */

public class AnnotatedInterceptorInjector implements InterceptorInjector {
    private final TraceContext traceContext;
    private final ProfilerPluginContext pluginContext;
    private final ByteCodeInstrumentor instrumentor;
    private final String interceptorName;
    private final Object[] providedArguments;


    public AnnotatedInterceptorInjector(TraceContext traceContext, ProfilerPluginContext pluginContext, ByteCodeInstrumentor instrumentor, String interceptorName, Object[] providedArguments) {
        this.traceContext = traceContext;
        this.pluginContext = pluginContext;
        this.instrumentor = instrumentor;
        this.interceptorName = interceptorName;
        this.providedArguments = providedArguments;
    }
    
    @Override
    public void edit(ClassLoader targetClassLoader, InstrumentClass targetClass, MethodInfo targetMethod) throws InstrumentException {
        Class<? extends Interceptor> interceptorType = TypeUtils.loadClass(targetClassLoader, interceptorName);
        
        InterceptorFactory factory = createInterceptorFactory(interceptorType);
        InterceptorInjector injector = createInterceptorInjector(interceptorType, factory);
        
        injector.edit(targetClassLoader, targetClass, targetMethod);
    }
    
    private InterceptorInjector createInterceptorInjector(Class<?> interceptorType, InterceptorFactory factory) {
        if (interceptorType.isAnnotationPresent(Singleton.class)) {
            return new SingletonInterceptorInjector(factory);
        } else {
            return new DefaultInterceptorInjector(factory);
        }
    }
    
    private InterceptorFactory createInterceptorFactory(Class<? extends Interceptor> interceptorType) {
        InterceptorFactory factory = new AnnotatedInterceptorFactory(traceContext, pluginContext, instrumentor, interceptorType, providedArguments);
        
        Scope scope = interceptorType.getAnnotation(Scope.class);
        
        if (scope != null) {
            String scopeName = scope.value();
            
            if (scopeName == null) {
                throw new PinpointException("@Scope must provide scope name. Interceptor class: " + interceptorName);
            }
            
            factory = new ScopedInterceptorFactory(factory, instrumentor, scopeName);
        }
        
        return factory;
    }
}
