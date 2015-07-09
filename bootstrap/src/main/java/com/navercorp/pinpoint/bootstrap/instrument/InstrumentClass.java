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

import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;

import java.util.List;

/**
 * @author emeroad
 * @author netspider
 */
public interface InstrumentClass {

    boolean isInterface();

    String getName();

    String getSuperClass();

    String[] getInterfaces();

    @Deprecated
    boolean insertCodeBeforeMethod(String methodName, String[] args, String code);

    @Deprecated
    boolean insertCodeAfterMethod(String methodName, String[] args, String code);

    @Deprecated
    int addAllConstructorInterceptor(Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    @Deprecated
    int addAllConstructorInterceptor(Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    int addConstructorInterceptor(String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    int addConstructorInterceptor(String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    int reuseInterceptor(String methodName, String[] args, int interceptorId) throws InstrumentException, NotFoundInstrumentException;

    int reuseInterceptor(String methodName, String[] args, int interceptorId, Type type) throws InstrumentException, NotFoundInstrumentException;


    int addInterceptor(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    int addGroupInterceptor(String methodName, String[] args, Interceptor interceptor, String scopeName) throws InstrumentException, NotFoundInstrumentException;

    int addGroupInterceptor(String methodName, String[] args, Interceptor interceptor, InterceptorGroupDefinition scopeDefinition) throws InstrumentException;

    int addGroupInterceptorIfDeclared(String methodName, String[] args, Interceptor interceptor, String scopeName) throws InstrumentException;

    int addGroupInterceptorIfDeclared(String methodName, String[] args, Interceptor interceptor, InterceptorGroupDefinition scopeDefinition) throws InstrumentException;

    int addInterceptor(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    void weave(String adviceClassName, ClassLoader classLoader) throws InstrumentException;

    boolean addDebugLogBeforeAfterMethod();

    boolean addDebugLogBeforeAfterConstructor();

    byte[] toBytecode() throws InstrumentException;

    Class<?> toClass() throws InstrumentException;

    /**
     * Use addTraceValue instead of this method.
     */
    @Deprecated
    void addTraceVariable(String variableName, String setterName, String getterName, String variableType, String initValue) throws InstrumentException;

    /**
     * Use addTraceValue instead of this method.
     */
    @Deprecated
    void addTraceVariable(String variableName, String setterName, String getterName, String variableType) throws InstrumentException;

    void addTraceValue(Class<?> traceValue, String initValue) throws InstrumentException;

    void addTraceValue(Class<?> traceValue) throws InstrumentException;

    boolean insertCodeAfterConstructor(String[] args, String code);

    boolean insertCodeBeforeConstructor(String[] args, String code);

    List<MethodInfo> getDeclaredMethods();

    List<MethodInfo> getDeclaredMethods(MethodFilter methodFilter);

    MethodInfo getDeclaredMethod(String name, String[] parameterTypes);

    MethodInfo getConstructor(String[] parameterTypes);

    public boolean isInterceptable();

    boolean hasDeclaredMethod(String methodName, String[] args);
    
    boolean hasConstructor(String[] parameterTypeArray);

    boolean hasMethod(String methodName, String[] parameterTypeArray, String returnType);
    
    boolean hasField(String name, String type);

    InstrumentClass getNestedClass(String className);

    void addGetter(String getterName, String fieldName, String fieldType) throws InstrumentException;
    
    void addGetter(Class<?> interfaceType, String fieldName) throws InstrumentException;

    /**
     * You should check that class already have Declared method.
     * If class already have method, this method throw exception. 
     */
    void addDelegatorMethod(String methodName, String[] args) throws InstrumentException;
}
