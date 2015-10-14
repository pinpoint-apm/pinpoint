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
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.interceptor.AfterInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AfterInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.AfterInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.AfterInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.AfterInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.AfterInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.AfterInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.BeforeInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.BeforeInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.BeforeInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.BeforeInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.BeforeInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.BeforeInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.BeforeInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Group;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectRecipe;
import com.navercorp.pinpoint.profiler.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.InterceptorArgumentProvider;

public class AnnotatedInterceptorFactory implements InterceptorFactory {
    private final Instrumentor pluginContext;
    
    public AnnotatedInterceptorFactory(Instrumentor pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public Interceptor getInterceptor(ClassLoader classLoader, String interceptorClassName, Object[] providedArguments, InterceptorGroup group, ExecutionPolicy policy, InstrumentClass target, InstrumentMethod targetMethod) {
        Class<? extends Interceptor> interceptorType = pluginContext.injectClass(classLoader, interceptorClassName);
        
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
        if (interceptor instanceof AroundInterceptor) {
            return new GroupedInterceptor((AroundInterceptor)interceptor, (AroundInterceptor)interceptor, group, policy);
        } else if (interceptor instanceof StaticAroundInterceptor) {
            return new GroupedStaticAroundInterceptor((StaticAroundInterceptor)interceptor, group, policy);
        } else if (interceptor instanceof AroundInterceptor5) {
            return new GroupedInterceptor5((BeforeInterceptor5)interceptor, (AfterInterceptor5)interceptor, group, policy);
        } else if (interceptor instanceof BeforeInterceptor5) {
            return new GroupedInterceptor5((BeforeInterceptor5)interceptor, null, group, policy);
        } else if (interceptor instanceof AfterInterceptor5) {
            return new GroupedInterceptor5(null, (AfterInterceptor5)interceptor, group, policy);
        } else if (interceptor instanceof AroundInterceptor4) {
            return new GroupedInterceptor4((BeforeInterceptor4)interceptor, (AfterInterceptor4)interceptor, group, policy);
        } else if (interceptor instanceof BeforeInterceptor4) {
            return new GroupedInterceptor4((BeforeInterceptor4)interceptor, null, group, policy);
        } else if (interceptor instanceof AfterInterceptor4) {
            return new GroupedInterceptor4(null, (AfterInterceptor4)interceptor, group, policy);
        } else if (interceptor instanceof AroundInterceptor3) {
            return new GroupedInterceptor3((BeforeInterceptor3)interceptor, (AfterInterceptor3)interceptor, group, policy);
        } else if (interceptor instanceof BeforeInterceptor3) {
            return new GroupedInterceptor3((BeforeInterceptor3)interceptor, null, group, policy);
        } else if (interceptor instanceof AfterInterceptor3) {
            return new GroupedInterceptor3(null, (AfterInterceptor3)interceptor, group, policy);
        } else if (interceptor instanceof AroundInterceptor2) {
            return new GroupedInterceptor2((BeforeInterceptor2)interceptor, (AfterInterceptor2)interceptor, group, policy);
        } else if (interceptor instanceof BeforeInterceptor2) {
            return new GroupedInterceptor2((BeforeInterceptor2)interceptor, null, group, policy);
        } else if (interceptor instanceof AfterInterceptor2) {
            return new GroupedInterceptor2(null, (AfterInterceptor2)interceptor, group, policy);
        } else if (interceptor instanceof AroundInterceptor1) {
            return new GroupedInterceptor1((BeforeInterceptor1)interceptor, (AfterInterceptor1)interceptor, group, policy);
        } else if (interceptor instanceof BeforeInterceptor1) {
            return new GroupedInterceptor1((BeforeInterceptor1)interceptor, null, group, policy);
        } else if (interceptor instanceof AfterInterceptor1) {
            return new GroupedInterceptor1(null, (AfterInterceptor1)interceptor, group, policy);
        } else if (interceptor instanceof AroundInterceptor0) {
            return new GroupedInterceptor0((BeforeInterceptor0)interceptor, (AfterInterceptor0)interceptor, group, policy);
        } else if (interceptor instanceof BeforeInterceptor0) {
            return new GroupedInterceptor0((BeforeInterceptor0)interceptor, null, group, policy);
        } else if (interceptor instanceof AfterInterceptor0) {
            return new GroupedInterceptor0(null, (AfterInterceptor0)interceptor, group, policy);
        } else if (interceptor instanceof BeforeInterceptor) {
            return new GroupedInterceptor((BeforeInterceptor)interceptor, null, group, policy);
        } else if (interceptor instanceof AfterInterceptor) {
            return new GroupedInterceptor(null, (AfterInterceptor)interceptor, group, policy);
        } else if (interceptor instanceof ApiIdAwareAroundInterceptor) {
            return new GroupedApiIdAwareAroundInterceptor((ApiIdAwareAroundInterceptor)interceptor, group, policy);
        }
        
        throw new IllegalArgumentException("Unexpected interceptor type: " + interceptor.getClass());
    }
}
