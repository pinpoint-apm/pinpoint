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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.instrument.interceptor.CaptureType;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinition;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorType;
import com.navercorp.pinpoint.profiler.interceptor.factory.AnnotatedInterceptorFactory;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaehong.kim
 */
public class ASMMethod implements InstrumentMethod {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final EngineComponent engineComponent;
    private final InstrumentContext pluginContext;
    private final ASMClass declaringClass;
    private final ASMMethodNodeAdapter methodNode;
    private final MethodDescriptor descriptor;


    public ASMMethod(EngineComponent engineComponent, InstrumentContext pluginContext, ASMClass declaringClass, MethodNode methodNode) {
        this(engineComponent, pluginContext, declaringClass, new ASMMethodNodeAdapter(JavaAssistUtils.javaNameToJvmName(declaringClass.getName()), methodNode));

    }

    public ASMMethod(EngineComponent engineComponent, InstrumentContext pluginContext, ASMClass declaringClass, ASMMethodNodeAdapter methodNode) {
        this.engineComponent = Assert.requireNonNull(engineComponent, "engineComponent");
        this.pluginContext = Assert.requireNonNull(pluginContext, "pluginContext");
        this.declaringClass = declaringClass;
        this.methodNode = methodNode;

        final String[] parameterVariableNames = this.methodNode.getParameterNames();
        final int lineNumber = this.methodNode.getLineNumber();

        final DefaultMethodDescriptor descriptor = new DefaultMethodDescriptor(declaringClass.getName(), methodNode.getName(), getParameterTypes(), parameterVariableNames);
        descriptor.setLineNumber(lineNumber);

        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return this.methodNode.getName();
    }

    @Override
    public String[] getParameterTypes() {
        return this.methodNode.getParameterTypes();
    }

    @Override
    public String getReturnType() {
        return this.methodNode.getReturnType();
    }

    @Override
    public int getModifiers() {
        return this.methodNode.getAccess();
    }

    @Override
    public boolean isConstructor() {
        return this.methodNode.isConstructor();
    }

    @Override
    public MethodDescriptor getDescriptor() {
        return this.descriptor;
    }

    private Class<? extends Interceptor> loadInterceptorClass(String interceptorClassName) throws InstrumentException {
        try {
            ClassLoader classLoader = this.declaringClass.getClassLoader();
            return pluginContext.injectClass(classLoader, interceptorClassName);
        } catch (Exception ex) {
            throw new InstrumentException(interceptorClassName + " not found Caused by:" + ex.getMessage(), ex);
        }
    }

    @Override
    public int addInterceptor(String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addInterceptor(interceptorClass);
    }

    @Override
    public int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addInterceptor(interceptorClass, constructorArgs);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scopeName, "scopeName");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, scopeName);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope interceptorScope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, interceptorScope);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, scopeName, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        final Class<? extends Interceptor> interceptorClass = pluginContext.injectClass(this.declaringClass.getClassLoader(), interceptorClassName);
        return addScopedInterceptor(interceptorClass, constructorArgs, scopeName);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        final Class<? extends Interceptor> interceptorClass = pluginContext.injectClass(this.declaringClass.getClassLoader(), interceptorClassName);
        return addScopedInterceptor(interceptorClass, constructorArgs, interceptorScope);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, constructorArgs, scopeName, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public void addInterceptor(int interceptorId) throws InstrumentException {
        final Interceptor interceptor = InterceptorRegistry.getInterceptor(interceptorId);
        try {
            addInterceptor0(interceptor, interceptorId);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptor.getClass().getName() + " to " + this.methodNode.getLongName(), e);
        }
    }

    // for internal api
    int addInterceptorInternal(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        if (interceptorClass == null) {
            throw new NullPointerException("interceptorClass");
        }
        final Interceptor interceptor = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        return addInterceptor0(interceptor);
    }

    private int addInterceptor0(Interceptor interceptor) {
        final int interceptorId = this.engineComponent.addInterceptor(interceptor);

        addInterceptor0(interceptor, interceptorId);
        return interceptorId;
    }

    private Interceptor newInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) {
        final ScopeFactory scopeFactory = this.engineComponent.getScopeFactory();

        final ScopeInfo scopeInfo = scopeFactory.newScopeInfo(pluginContext, interceptorClass, interceptorScope, executionPolicy);
        return createInterceptor(interceptorClass, constructorArgs, scopeInfo);
    }


    private Interceptor createInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, ScopeInfo scopeInfo) {
        // exception handling.
        ObjectBinderFactory objectBinderFactory = this.engineComponent.getObjectBinderFactory();
        final AnnotatedInterceptorFactory factory = objectBinderFactory.newAnnotatedInterceptorFactory(this.pluginContext);
        final Interceptor interceptor = factory.newInterceptor(interceptorClass, constructorArgs, scopeInfo, this.declaringClass, this);
        return interceptor;
    }

    private void addInterceptor0(Interceptor interceptor, int interceptorId) {
        if (interceptor == null) {
            throw new NullPointerException("interceptor");
        }

        final InterceptorDefinition interceptorDefinition = this.engineComponent.createInterceptorDefinition(interceptor.getClass());
        final Class<?> interceptorClass = interceptorDefinition.getInterceptorClass();
        final CaptureType captureType = interceptorDefinition.getCaptureType();
        if (this.methodNode.hasInterceptor()) {
            logger.warn("Skip adding interceptor. 'already intercepted method' class={}, interceptor={}", this.declaringClass.getName(), interceptorClass.getName());
            return;
        }

        if (this.methodNode.isAbstract() || this.methodNode.isNative()) {
            logger.warn("Skip adding interceptor. 'abstract or native method' class={}, interceptor={}", this.declaringClass.getName(), interceptorClass.getName());
            return;
        }

        int apiId = -1;
        if (interceptorDefinition.getInterceptorType() == InterceptorType.API_ID_AWARE) {
            apiId = this.engineComponent.cacheApi(this.descriptor);
        }

        // add before interceptor.
        if (isBeforeInterceptor(captureType) && interceptorDefinition.getBeforeMethod() != null) {
            this.methodNode.addBeforeInterceptor(interceptorId, interceptorDefinition, apiId);
            this.declaringClass.setModified(true);
        } else {
            if (isDebug) {
                logger.debug("Skip adding before interceptorDefinition because the interceptorDefinition doesn't have before method: {}", interceptorClass.getName());
            }
        }

        // add after interface.
        if (isAfterInterceptor(captureType) && interceptorDefinition.getAfterMethod() != null) {
            this.methodNode.addAfterInterceptor(interceptorId, interceptorDefinition, apiId);
            this.declaringClass.setModified(true);
        } else {
            if (isDebug) {
                logger.debug("Skip adding after interceptor because the interceptor doesn't have after method: {}", interceptorClass.getName());
            }
        }
    }

    private boolean isBeforeInterceptor(CaptureType captureType) {
        return CaptureType.BEFORE == captureType || CaptureType.AROUND == captureType;
    }

    private boolean isAfterInterceptor(CaptureType captureType) {
        return CaptureType.AFTER == captureType || CaptureType.AROUND == captureType;
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");

        final Interceptor interceptor = newInterceptor(interceptorClass, null, null, null);
        return addInterceptor0(interceptor);
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");

        final Interceptor interceptor = newInterceptor(interceptorClass, constructorArgs, null, null);
        return addInterceptor0(interceptor);
    }


    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final Interceptor interceptor = newInterceptor(interceptorClass, null, interceptorScope, null);
        return addInterceptor0(interceptor);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(interceptorScope, "interceptorScope");

        final Interceptor interceptor = newInterceptor(interceptorClass, null, interceptorScope, null);
        return addInterceptor0(interceptor);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final Interceptor interceptor = newInterceptor(interceptorClass, null, interceptorScope, executionPolicy);
        return addInterceptor0(interceptor);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");

        final Interceptor interceptor = newInterceptor(interceptorClass, null, interceptorScope, executionPolicy);
        return addInterceptor0(interceptor);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final Interceptor interceptor = newInterceptor(interceptorClass, constructorArgs, interceptorScope, null);
        return addInterceptor0(interceptor);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");

        final Interceptor interceptor = newInterceptor(interceptorClass, constructorArgs, interceptorScope, null);
        return addInterceptor0(interceptor);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final Interceptor interceptor = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        return addInterceptor0(interceptor);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");

        final Interceptor interceptor = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        return addInterceptor0(interceptor);
    }


}