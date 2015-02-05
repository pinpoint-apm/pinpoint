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
package com.navercorp.pinpoint.bootstrap.plugin.editor;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;

/**
 * @author Jongho Moon
 *
 */
public class SingletonInterceptorInjector implements InterceptorInjector {
    private final InterceptorFactory factory;
    private int id = -1;

    public SingletonInterceptorInjector(InterceptorFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public void edit(ClassLoader targetClassLoader, InstrumentClass targetClass, MethodInfo targetMethod) throws InstrumentException {
        if (id == -1) {
            Interceptor interceptor = factory.getInterceptor(targetClassLoader, targetClass, targetMethod);
            
            if (targetMethod.isConstructor()) {
                id = targetClass.addConstructorInterceptor(targetMethod.getParameterTypes(), interceptor);
            } else {
                id = targetClass.addInterceptor(targetMethod.getName(), targetMethod.getParameterTypes(), interceptor);
            }
        } else {
            if (targetMethod.isConstructor()) {
                // InstruemtnClass does not have reuseConstructorInterceptor(). Maybe nobody needs it. Don't bother adding unnecessary method.
                throw new IllegalArgumentException("Reusing constructor interceptor is not supported");
            } else {
                targetClass.reuseInterceptor(targetMethod.getName(), targetMethod.getParameterTypes(), id);
            }
        }
    }
}
