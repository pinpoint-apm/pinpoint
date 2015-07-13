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
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectRecipe;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.plugin.objectfactory.InterceptorArgumentProvider;

public class AnnotatedInterceptorFactory implements InterceptorFactory {
    private final DefaultProfilerPluginContext pluginContext;
    
    public AnnotatedInterceptorFactory(DefaultProfilerPluginContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public Interceptor getInterceptor(ClassLoader classLoader, String interceptorClassName, Object[] providedArguments, InterceptorGroup group, ExecutionPolicy policy, InstrumentClass target, MethodInfo targetMethod) {
        Class<? extends Interceptor> interceptorType = pluginContext.getClassInjector().loadClass(classLoader, interceptorClassName);
        
        if (group == null) {
            Group interceptorGroup = interceptorType.getAnnotation(Group.class);
            
            if (interceptorGroup != null) {
                String groupName = interceptorGroup.value();
                group = pluginContext.getInterceptorGroup(groupName);
                policy = interceptorGroup.executionPolicy();
            }
        }
        
        AutoBindingObjectFactory factory = new AutoBindingObjectFactory(pluginContext, classLoader);
        ObjectRecipe recipe = ObjectRecipe.byConstructor(interceptorClassName, providedArguments);
        InterceptorArgumentProvider interceptorArgumentProvider = new InterceptorArgumentProvider(pluginContext.getTraceContext(), group, target, targetMethod);
        
        Interceptor interceptor = (Interceptor)factory.createInstance(recipe, interceptorArgumentProvider);
        
        if (group != null) {
            interceptor = wrapByGroup(interceptor, group, policy == null ? ExecutionPolicy.BOUNDARY : policy);
        }
        
        return interceptor;
    }
    
    private Interceptor wrapByGroup(Interceptor interceptor, InterceptorGroup group, ExecutionPolicy policy) {
        if (interceptor instanceof SimpleAroundInterceptor) {
            return new GroupedSimpleAroundInterceptor((SimpleAroundInterceptor)interceptor, group, policy);
        }  else if (interceptor instanceof StaticAroundInterceptor) {
            return new GroupedStaticAroundInterceptor((StaticAroundInterceptor)interceptor, group, policy);
        }
        
        throw new IllegalArgumentException("Unexpected interceptor type: " + interceptor.getClass());
    }
}
