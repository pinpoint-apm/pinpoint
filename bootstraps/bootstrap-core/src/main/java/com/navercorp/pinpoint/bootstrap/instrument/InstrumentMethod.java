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

package com.navercorp.pinpoint.bootstrap.instrument;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

/**
 * @author emeroad
 */
public interface InstrumentMethod {
    String getName();

    String[] getParameterTypes();
    
    String getReturnType();

    int getModifiers();
    
    boolean isConstructor();
    
    MethodDescriptor getDescriptor();

    //-------------------- Plugin V2 Api  ----------------------------------
    int addInterceptor(Class<? extends Interceptor> interceptorClass) throws InstrumentException;

    int addInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException;

    int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName) throws InstrumentException;

    int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope) throws InstrumentException;


    int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException;


    int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName) throws InstrumentException;

    int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException;


    int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException;


    void addInterceptor(int interceptorId) throws InstrumentException;

    Class<? extends Interceptor> loadInterceptorClass(String interceptorClassName) throws InstrumentException;
}
