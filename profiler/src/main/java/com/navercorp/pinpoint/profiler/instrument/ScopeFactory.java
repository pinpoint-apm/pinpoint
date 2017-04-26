/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ScopeFactory {

    public ScopeFactory() {
    }

    public ScopeInfo newScopeInfo(ClassLoader classLoader, InstrumentContext pluginContext, String interceptorClassName, InterceptorScope scope, ExecutionPolicy policy) {

        if (scope == null) {
            final Class<? extends Interceptor> interceptorClass = pluginContext.injectClass(classLoader, interceptorClassName);
            final Scope scopeAnnotation = interceptorClass.getAnnotation(Scope.class);
            if (scopeAnnotation != null) {
                return newScopeInfoByAnnotation(pluginContext, scopeAnnotation);
            }
        }
        policy = getExecutionPolicy(scope, policy);

        return new ScopeInfo(scope, policy);
    }

    private ScopeInfo newScopeInfoByAnnotation(InstrumentContext pluginContext, Scope scope) {
        final String scopeName = scope.value();
        final InterceptorScope interceptorScope = pluginContext.getInterceptorScope(scopeName);

        final ExecutionPolicy policy = scope.executionPolicy();
        return new ScopeInfo(interceptorScope, policy);
    }

    private ExecutionPolicy getExecutionPolicy(InterceptorScope scope, ExecutionPolicy policy) {
        if (scope == null) {
            policy = null;
        } else if (policy == null) {
            policy = ExecutionPolicy.BOUNDARY;
        }
        return policy;
    }

}
