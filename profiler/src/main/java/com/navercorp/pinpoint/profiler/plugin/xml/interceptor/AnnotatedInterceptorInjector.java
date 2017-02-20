/*
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
package com.navercorp.pinpoint.profiler.plugin.xml.interceptor;

import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.MethodRecipe;

/**
 * @author Jongho Moon
 *
 */

public class AnnotatedInterceptorInjector implements MethodRecipe {
    private final InstrumentContext pluginContext;
    
    protected final String interceptorClassName;
    private final Object[] providedArguments;
    
    private final String scopeName;
    private final ExecutionPolicy executionPolicy;
    
    public AnnotatedInterceptorInjector(InstrumentContext pluginContext, String interceptorName, Object[] constructorArguments, String scopeName, ExecutionPolicy executionPolicy) {
        this.pluginContext = pluginContext;
        this.interceptorClassName = interceptorName;
        this.providedArguments = constructorArguments;
        this.scopeName = scopeName;
        this.executionPolicy = executionPolicy; 
    }
    
    @Override
    public void edit(ClassLoader targetClassLoader, InstrumentClass targetClass, InstrumentMethod targetMethod) throws Exception {
        inject(targetMethod);
    }

    int inject(InstrumentMethod targetMethod) throws InstrumentException {
        final InterceptorScope scope = getScope();
        return targetMethod.addScopedInterceptor(interceptorClassName, providedArguments, scope, executionPolicy);
    }

    private InterceptorScope getScope() {
        if (scopeName == null) {
            return null;
        } else {
            return pluginContext.getInterceptorScope(scopeName);
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
        
        if (scopeName != null) {
            builder.append(", scope=");
            builder.append(scopeName);
            builder.append(", executionPolicy=");
            builder.append(executionPolicy);
        }
        
        builder.append(']');
        return builder.toString();
    }
}
