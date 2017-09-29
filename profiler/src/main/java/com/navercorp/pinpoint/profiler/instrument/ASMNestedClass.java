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
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMNestedClass implements InstrumentClass {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ASMClass aClass;

    public ASMNestedClass(ObjectBinderFactory objectBinderFactory, final InstrumentContext pluginContext, final InterceptorRegistryBinder interceptorRegistryBinder, ApiMetaDataService apiMetaDataService, final ClassLoader classLoader, final ClassNode classNode) {
        this.aClass = new ASMClass(objectBinderFactory, pluginContext, interceptorRegistryBinder, apiMetaDataService, classLoader, classNode);
    }

    public ASMNestedClass(ObjectBinderFactory objectBinderFactory, final InstrumentContext pluginContext, final InterceptorRegistryBinder interceptorRegistryBinder, ApiMetaDataService apiMetaDataService, final ClassLoader classLoader, final ASMClassNodeAdapter classNodeAdapter) {
        this.aClass = new ASMClass(objectBinderFactory, pluginContext, interceptorRegistryBinder, apiMetaDataService, classLoader, classNodeAdapter);
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
    public void addGetter(String getterTypeName, String fieldName) throws InstrumentException {
        // nothing.
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName) throws InstrumentException {
        // nothing.
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName, boolean removeFinal) throws InstrumentException {
        // nothing.
    }

    @Override
    public int addInterceptor(String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        return 0;
    }

    @Override
    public int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return 0;
    }

    @Override
    public int addInterceptor(MethodFilter filter, String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        return 0;
    }

    @Override
    public int addInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return 0;
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
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