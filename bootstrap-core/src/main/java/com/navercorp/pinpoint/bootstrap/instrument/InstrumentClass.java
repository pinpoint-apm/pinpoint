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

import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;

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


    public boolean isInterceptable();
    
    boolean hasConstructor(String... parameterTypes);

    boolean hasDeclaredMethod(String methodName, String... parameterTypes);
    
    boolean hasMethod(String methodName, String... parameterTypes);
    
    boolean hasEnclosingMethod(String methodName, String... parameterTypes);
    
    boolean hasField(String name, String type);
    
    boolean hasField(String name);

    
    void weave(String adviceClassName) throws InstrumentException;

    void addField(String accessorTypeName) throws InstrumentException;

    void addGetter(String getterTypeName, String fieldName) throws InstrumentException;


    int addInterceptor(String interceptorClassName) throws InstrumentException;

    int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException;

    int addInterceptor(MethodFilter filter, String interceptorClassName) throws InstrumentException;

    int addInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs) throws InstrumentException;


    int addGroupedInterceptor(String interceptorClassName, Object[] constructorArgs, String groupName) throws InstrumentException;

    int addGroupedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorGroup group) throws InstrumentException;


    int addGroupedInterceptor(String interceptorClassName, Object[] constructorArgs, String groupName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addGroupedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorGroup group, ExecutionPolicy executionPolicy) throws InstrumentException;


    int addGroupedInterceptor(String interceptorClassName, String groupName) throws InstrumentException;

    int addGroupedInterceptor(String interceptorClassName, InterceptorGroup group) throws InstrumentException;


    int addGroupedInterceptor(String interceptorClassName, String groupName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addGroupedInterceptor(String interceptorClassName, InterceptorGroup group, ExecutionPolicy executionPolicy) throws InstrumentException;


    int addGroupedInterceptor(MethodFilter filter, String interceptorClassName, String groupName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addGroupedInterceptor(MethodFilter filter, String interceptorClassName, InterceptorGroup group, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addGroupedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, String groupName, ExecutionPolicy executionPolicy) throws InstrumentException;

    int addGroupedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, InterceptorGroup group, ExecutionPolicy executionPolicy) throws InstrumentException;

    /**
     * You should check that class already have Declared method.
     * If class already have method, this method throw exception. 
     */
    InstrumentMethod addDelegatorMethod(String methodName, String... paramTypes) throws InstrumentException;
    
    byte[] toBytecode() throws InstrumentException;
}
