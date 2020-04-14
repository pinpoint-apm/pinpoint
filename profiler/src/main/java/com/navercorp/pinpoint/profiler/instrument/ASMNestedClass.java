/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.ClassFilter;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMNestedClass implements InstrumentClass {

    private final ASMClass aClass;

    public ASMNestedClass(EngineComponent engineComponent, final InstrumentContext pluginContext, final ASMClassNodeAdapter classNodeAdapter) {
        this.aClass = new ASMClass(engineComponent, pluginContext, classNodeAdapter);
    }

    public ClassLoader getClassLoader() {
        return this.aClass.getClassLoader();
    }

    @Override
    public boolean isInterceptable() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return this.aClass.isInterface();
    }

    @Override
    public String getName() {
        return this.aClass.getName();
    }

    @Override
    public String getSuperClass() {
        return this.aClass.getSuperClass();
    }

    @Override
    public String[] getInterfaces() {
        return this.aClass.getInterfaces();
    }

    @Override
    public InstrumentMethod getDeclaredMethod(String name, String... parameterTypes) {
        return null;
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods() {
        return Collections.emptyList();
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods(MethodFilter methodFilter) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasDeclaredMethod(String methodName, String... args) {
        return this.aClass.hasDeclaredMethod(methodName, args);
    }

    @Override
    public boolean hasMethod(String methodName, String... parameterTypes) {
        return this.aClass.hasMethod(methodName, parameterTypes);
    }

    @Override
    public boolean hasEnclosingMethod(String methodName, String... parameterTypes) {
        return this.aClass.hasEnclosingMethod(methodName, parameterTypes);
    }

    @Override
    public InstrumentMethod getConstructor(String... parameterTypes) {
        return null;
    }

    @Override
    public List<InstrumentMethod> getDeclaredConstructors() {
        return null;
    }

    @Override
    public boolean hasConstructor(String... parameterTypeArray) {
        return this.aClass.hasConstructor(parameterTypeArray);
    }

    @Override
    public boolean hasField(String name, String type) {
        return this.aClass.hasField(name, type);
    }

    @Override
    public boolean hasField(String name) {
        return this.aClass.hasField(name);
    }

    @Override
    public void weave(String adviceClassInternalName) throws InstrumentException {
        // nothing.
    }

    @Override
    public InstrumentMethod addDelegatorMethod(String methodName, String... paramTypes) throws InstrumentException {
        return null;
    }

    @Override
    public void addField(String accessorTypeName) throws InstrumentException {
        // nothing.
    }

    @Override
    public void addField(Class<?> accessorClass) throws InstrumentException {

    }

    @Override
    public void addGetter(String getterTypeName, String fieldName) throws InstrumentException {
        // nothing.
    }

    @Override
    public void addGetter(Class<?> getterClass, String fieldName) throws InstrumentException {
        // nothing.
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName) throws InstrumentException {
        // nothing.
    }

    @Override
    public void addSetter(Class<?> setterClass, String fieldName) throws InstrumentException {
        // nothing.
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName, boolean removeFinal) throws InstrumentException {
        // nothing.
    }

    @Override
    public void addSetter(Class<?> setterClass, String fieldName, boolean removeFinal) throws InstrumentException {
        // nothing.
    }

    @Override
    public int addInterceptor(String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        return 0;
    }

    @Override
    public int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scopeName, "scopeName");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scope, "scope");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scope, "scope");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scope, "scope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scope, "scope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addInterceptor(MethodFilter filter, String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        return 0;
    }

    @Override
    public int addInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scope, "scope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scope, "scope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        return 0;
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        return 0;
    }

    @Override
    public int addInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        return 0;
    }

    @Override
    public int addInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        return 0;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        return 0;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scope, "scope");
        return 0;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scope, "scope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        return 0;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scope, "scope");
        return 0;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scope, "scope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scope, "scope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scope, "scope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return 0;
    }

    @Override
    public List<InstrumentClass> getNestedClasses(ClassFilter filter) {
        return this.aClass.getNestedClasses(filter);
    }

    @Override
    public byte[] toBytecode() {
        return null;
    }
}