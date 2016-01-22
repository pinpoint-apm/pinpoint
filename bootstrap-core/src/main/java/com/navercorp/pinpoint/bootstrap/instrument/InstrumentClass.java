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

import java.util.List;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

/**
 * @author emeroad
 * @author netspider
 */
public interface InstrumentClass {

    boolean isInterface();

    String getName();

    String getSuperClass();

    String[] getInterfaces();
    
    InstrumentMethod getConstructor(String... parameterTypes);

    List<InstrumentMethod> getDeclaredMethods();

    List<InstrumentMethod> getDeclaredMethods(MethodFilter filter);

    InstrumentMethod getDeclaredMethod(String name, String... parameterTypes);

    List<InstrumentClass> getNestedClasses(ClassFilter filter);    
    
    ClassLoader getClassLoader();


    boolean isInterceptable();
    
    boolean hasConstructor(String... parameterTypes);

    boolean hasDeclaredMethod(String methodName, String... parameterTypes);
    
    boolean hasMethod(String methodName, String... parameterTypes);
    
    boolean hasEnclosingMethod(String methodName, String... parameterTypes);
    
    boolean hasField(String name, String type);
    
    boolean hasField(String name);

    
    void weave(String adviceClassName) throws InstrumentException;

    void addField(String accessorTypeName) throws InstrumentException;

    void addGetter(String getterTypeName, String fieldName) throws InstrumentException;

    void addSetter(String setterTypeName, String fieldName) throws InstrumentException;

    void addSetter(String setterTypeName, String fieldName, boolean removeFinal) throws InstrumentException;


    int addInterceptor(String interceptorClassName) throws InstrumentException;

    int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException;

    int addInterceptor(MethodFilter filter, String interceptorClassName) throws InstrumentException;

    int addInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs) throws InstrumentException;


    int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName) throws InstrumentException;

    int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope) throws InstrumentException;


    int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException;


    int addScopedInterceptor(String interceptorClassName, String scopeName) throws InstrumentException;

    int addScopedInterceptor(String interceptorClassName, InterceptorScope scope) throws InstrumentException;


    int addScopedInterceptor(String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addScopedInterceptor(String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException;


    int addScopedInterceptor(MethodFilter filter, String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addScopedInterceptor(MethodFilter filter, String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException;

    /**
     * You should check that class already have Declared method.
     * If class already have method, this method throw exception. 
     */
    InstrumentMethod addDelegatorMethod(String methodName, String... paramTypes) throws InstrumentException;
    
    byte[] toBytecode() throws InstrumentException;
}
