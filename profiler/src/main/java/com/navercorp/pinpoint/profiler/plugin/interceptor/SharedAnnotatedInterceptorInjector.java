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
package com.navercorp.pinpoint.profiler.plugin.interceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableMethod;
import com.navercorp.pinpoint.profiler.plugin.transformer.MethodRecipe;

/**
 * @author Jongho Moon
 *
 */

public class SharedAnnotatedInterceptorInjector implements MethodRecipe {
    private final AnnotatedInterceptorInjector injector;
    private final ConcurrentMap<ClassLoader, Integer> interceptorIdMap = new ConcurrentHashMap<ClassLoader, Integer>();
    
    public SharedAnnotatedInterceptorInjector(AnnotatedInterceptorInjector injector) {
        this.injector = injector;
    }
    
    @Override
    public void edit(ClassLoader targetClassLoader, InstrumentableClass targetClass, InstrumentableMethod targetMethod) throws Exception {
        Integer interceptorId = interceptorIdMap.get(targetClassLoader);
        
        if (interceptorId == null) {
            interceptorId = injector.inject(targetClassLoader, targetClass, targetMethod);
            interceptorIdMap.put(targetClassLoader, interceptorId);
        } else {
            if (targetMethod.isConstructor()) {
                // InstrumentableClass does not have reuseConstructorInterceptor(). Maybe nobody needs it. Don't bother adding unnecessary method.
                throw new IllegalArgumentException("Reusing constructor interceptor is not supported");
            } else {
                targetClass.reuseInterceptor(targetMethod.getName(), targetMethod.getParameterTypes(), interceptorId);
            }
        }
    }

    @Override
    public String toString() {
        return "SharedAnnotatedInterceptorInjector[" + injector + "]";
    }
}
