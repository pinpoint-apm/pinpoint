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

package com.navercorp.pinpoint.profiler.plugin.interceptor;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.plugin.interceptor.InterceptorGroup;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.objectfactory.AutoBindingObjectFactory;

public class AnnotatedInterceptorFactory implements InterceptorFactory {
    private final DefaultProfilerPluginContext pluginContext;
    private final InterceptorGroup group;
    
    private final Class<? extends Interceptor> interceptorType;
    private final Object[] providedValues;
    
    
    public AnnotatedInterceptorFactory(DefaultProfilerPluginContext pluginContext, InterceptorGroup group, Class<? extends Interceptor> interceptorType, Object[] providedArguments) {
        this.pluginContext = pluginContext;
        this.group = group;
        this.interceptorType = interceptorType;
        this.providedValues = providedArguments;
    }

    @Override
    public Interceptor getInterceptor(ClassLoader classLoader, InstrumentClass target, MethodInfo targetMethod) {
        AutoBindingObjectFactory<Interceptor> factory = new AutoBindingObjectFactory<Interceptor>(pluginContext, group, target, targetMethod, providedValues);
        return factory.createInstance(interceptorType);
    }
}
