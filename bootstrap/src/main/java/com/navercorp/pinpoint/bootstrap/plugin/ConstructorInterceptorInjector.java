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

package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;

public class ConstructorInterceptorInjector implements Injector {
    private final String[] targetParameterTypes;
    private final InterceptorFactory factory;
    
    public ConstructorInterceptorInjector(String[] targetParameterTypes, InterceptorFactory factory) {
        this.targetParameterTypes = targetParameterTypes;
        this.factory = factory;
    }

    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        MethodInfo targetMethod = target.getConstructor(targetParameterTypes);
        Interceptor interceptor = factory.getInterceptor(classLoader, target, targetMethod);
        target.addConstructorInterceptor(targetParameterTypes, interceptor);
    }
}
