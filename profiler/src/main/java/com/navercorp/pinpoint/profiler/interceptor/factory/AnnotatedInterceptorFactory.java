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

package com.navercorp.pinpoint.profiler.interceptor.factory;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.InterceptorArgumentProvider;

public class AnnotatedInterceptorFactory implements InterceptorFactory {
    private final InstrumentContext pluginContext;
    
    public AnnotatedInterceptorFactory(InstrumentContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public Interceptor getInterceptor(ClassLoader classLoader, String interceptorClassName, Object[] providedArguments, InterceptorScope scope, ExecutionPolicy policy, InstrumentClass target, InstrumentMethod targetMethod) {
        Class<? extends Interceptor> interceptorType = pluginContext.injectClass(classLoader, interceptorClassName);
        
        if (scope == null) {
            Scope interceptorScope = interceptorType.getAnnotation(Scope.class);
            
            if (interceptorScope != null) {
                String scopeName = interceptorScope.value();
                scope = pluginContext.getInterceptorScope(scopeName);
                policy = interceptorScope.executionPolicy();
            }
        }
        
        AutoBindingObjectFactory factory = new AutoBindingObjectFactory(pluginContext, classLoader);
        ObjectFactory objectFactory = ObjectFactory.byConstructor(interceptorClassName, providedArguments);
        InterceptorArgumentProvider interceptorArgumentProvider = new InterceptorArgumentProvider(pluginContext.getTraceContext(), scope, target, targetMethod);
        
        Interceptor interceptor = (Interceptor)factory.createInstance(objectFactory, interceptorArgumentProvider);
        
        if (scope != null) {
            interceptor = wrapByScope(interceptor, scope, policy == null ? ExecutionPolicy.BOUNDARY : policy);
        }
        
        return interceptor;
    }
    
    private Interceptor wrapByScope(Interceptor interceptor, InterceptorScope scope, ExecutionPolicy policy) {
        if (interceptor instanceof AroundInterceptor) {
            return new ScopedInterceptor((AroundInterceptor)interceptor, scope, policy);
        } else if (interceptor instanceof StaticAroundInterceptor) {
            return new ScopedStaticAroundInterceptor((StaticAroundInterceptor)interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor5) {
            return new ScopedInterceptor5((AroundInterceptor5)interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor4) {
            return new ScopedInterceptor4((AroundInterceptor4)interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor3) {
            return new ScopedInterceptor3((AroundInterceptor3)interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor2) {
            return new ScopedInterceptor2((AroundInterceptor2)interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor1) {
            return new ScopedInterceptor1((AroundInterceptor1)interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor0) {
            return new ScopedInterceptor0((AroundInterceptor0)interceptor, scope, policy);
        } else if (interceptor instanceof ApiIdAwareAroundInterceptor) {
            return new ScopedApiIdAwareAroundInterceptor((ApiIdAwareAroundInterceptor)interceptor, scope, policy);
        }
        
        throw new IllegalArgumentException("Unexpected interceptor type: " + interceptor.getClass());
    }
}
