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

import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.transformer.MethodRecipe;

/**
 * @author Jongho Moon
 *
 */

public class AnnotatedInterceptorInjector implements MethodRecipe {
    private final DefaultProfilerPluginContext pluginContext;
    
    protected final String interceptorClassName;
    private final Object[] providedArguments;
    
    private final String groupName;
    private final ExecutionPolicy executionPoint;
    
    public AnnotatedInterceptorInjector(DefaultProfilerPluginContext pluginContext, String interceptorName, Object[] constructorArguments, String groupName, ExecutionPolicy executionPoint) {
        this.pluginContext = pluginContext;
        this.interceptorClassName = interceptorName;
        this.providedArguments = constructorArguments;
        this.groupName = groupName;
        this.executionPoint = executionPoint; 
    }
    
    @Override
    public void edit(ClassLoader targetClassLoader, InstrumentClass targetClass, MethodInfo targetMethod) throws Exception {
        inject(targetClassLoader, targetClass, targetMethod);
    }

    int inject(ClassLoader targetClassLoader, InstrumentClass targetClass, MethodInfo targetMethod) throws Exception {
        InterceptorFactory factory = new AnnotatedInterceptorFactory(pluginContext);
        Interceptor interceptor = factory.getInterceptor(targetClassLoader, interceptorClassName, providedArguments, null, null, targetClass, targetMethod);
        
        if (targetMethod.isConstructor()) {
            return targetClass.addConstructorInterceptor(targetMethod.getParameterTypes(), interceptor);
        } else {
            return targetClass.addInterceptor(targetMethod.getName(), targetMethod.getParameterTypes(), interceptor);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AnnotatedInterceptorInjector [interceptorClass=");
        builder.append(interceptorClassName);
        
        if (providedArguments != null && providedArguments.length != 0) {
            builder.append(", constructorArguments=");
            builder.append(Arrays.toString(providedArguments));
        }
        
        if (groupName != null) {
            builder.append(", group=");
            builder.append(groupName);
            builder.append(", executionPolicy=");
            builder.append(executionPoint);
        }
        
        builder.append(']');
        return builder.toString();
    }
}
