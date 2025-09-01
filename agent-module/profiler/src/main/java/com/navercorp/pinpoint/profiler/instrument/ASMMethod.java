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
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.profiler.context.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.instrument.interceptor.CaptureType;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinition;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorHolderIdGenerator;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorType;
import com.navercorp.pinpoint.profiler.interceptor.factory.AnnotatedInterceptorFactory;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ASMMethod implements InstrumentMethod {

    private final Logger logger = LogManager.getLogger(this.getClass());
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
        this.engineComponent = Objects.requireNonNull(engineComponent, "engineComponent");
        this.pluginContext = Objects.requireNonNull(pluginContext, "pluginContext");
        this.declaringClass = declaringClass;
        this.methodNode = methodNode;

        final String[] parameterVariableNames = this.methodNode.getParameterNames();
        final int lineNumber = this.methodNode.getLineNumber();

        this.descriptor = new DefaultMethodDescriptor(declaringClass.getName(), methodNode.getName(), getParameterTypes(), parameterVariableNames, lineNumber);
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

    public Class<? extends Interceptor> loadInterceptorClass(String interceptorClassName) throws InstrumentException {
        try {
            ClassLoader classLoader = this.declaringClass.getClassLoader();
            return pluginContext.injectClass(classLoader, interceptorClassName);
        } catch (Exception ex) {
            throw new InstrumentException(interceptorClassName + " not found Caused by:" + ex.getMessage(), ex);
        }
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");

        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, null, null, null);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");

        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, constructorArgs, null, null);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, null, interceptorScope, null);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(interceptorScope, "interceptorScope");

        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, null, interceptorScope, null);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");
        Objects.requireNonNull(executionPolicy, "executionPolicy");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, null, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        Objects.requireNonNull(executionPolicy, "executionPolicy");

        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, null, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(scopeName, "scopeName");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, constructorArgs, interceptorScope, null);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(interceptorScope, "interceptorScope");

        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, constructorArgs, interceptorScope, null);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(scopeName, "scopeName");
        Objects.requireNonNull(executionPolicy, "executionPolicy");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        Objects.requireNonNull(executionPolicy, "executionPolicy");

        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    @Override
    public void addInterceptor(int interceptorId) throws InstrumentException {
        InterceptorHolderIdGenerator interceptorHolderIdGenerator = engineComponent.getInterceptorHolderIdGenerator();
        final boolean isBootstrapInterceptorHolder = interceptorHolderIdGenerator.isBootstrapInterceptorHolder(interceptorId);
        if (isBootstrapInterceptorHolder) {
            logger.warn("Invalid using for bootstrap interceptor. interceptorId:{}", interceptorId);
        }

        final ASMInterceptorHolder interceptorHolder = new ASMInterceptorHolder(interceptorId, isBootstrapInterceptorHolder);
        final Class<? extends Interceptor> interceptorClass = interceptorHolder.loadInterceptorClass(this.declaringClass.getClassLoader());
        addInterceptor0(interceptorClass, interceptorHolder);
    }

    // for internal api
    int addInterceptorInternal(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");

        final ASMInterceptorHolder interceptorHolder = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorHolder);
        return interceptorHolder.getInterceptorId();
    }

    private ASMInterceptorHolder newInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        final ScopeFactory scopeFactory = this.engineComponent.getScopeFactory();
        final ScopeInfo scopeInfo = scopeFactory.newScopeInfo(pluginContext, interceptorClass, interceptorScope, executionPolicy);
        return createInterceptor(interceptorClass, constructorArgs, scopeInfo);
    }

    private ASMInterceptorHolder createInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, ScopeInfo scopeInfo) throws InstrumentException {
        final ObjectBinderFactory objectBinderFactory = this.engineComponent.getObjectBinderFactory();
        final AnnotatedInterceptorFactory factory = objectBinderFactory.newAnnotatedInterceptorFactory(this.pluginContext);
        final InterceptorHolderIdGenerator interceptorHolderIdGenerator = engineComponent.getInterceptorHolderIdGenerator();
        return ASMInterceptorHolder.create(interceptorHolderIdGenerator, declaringClass.getClassLoader(), factory, interceptorClass, constructorArgs, scopeInfo, descriptor);
    }

    private void addInterceptor0(Class<? extends Interceptor> interceptorClass, ASMInterceptorHolder interceptorHolder) {
        Objects.requireNonNull(interceptorClass, "interceptorClass");

        if(interceptorHolder.isEmpty()) {
            logger.warn("Skip adding interceptor. 'interceptorHolder empty' class={}, interceptor={}", this.declaringClass.getName(), interceptorClass.getName());
            return;
        }

        final InterceptorDefinition interceptorDefinition = this.engineComponent.createInterceptorDefinition(interceptorClass);
        final CaptureType captureType = interceptorDefinition.getCaptureType();
        if (this.methodNode.hasInterceptor()) {
            logger.warn("Skip adding interceptor. 'already intercepted method' class={}, interceptor={}", this.declaringClass.getName(), interceptorClass.getName());
            return;
        }

        if (this.methodNode.isAbstract() || this.methodNode.isNative()) {
            logger.info("Skip adding interceptor. 'abstract or native method' class={}, interceptor={}", this.declaringClass.getName(), interceptorClass.getName());
            return;
        }

        int apiId = 0;
        if (interceptorDefinition.getInterceptorType() == InterceptorType.API_ID_AWARE) {
            apiId = this.engineComponent.cacheApi(this.descriptor);
        }

        // add before interceptor.
        if (isBeforeInterceptor(captureType) && interceptorDefinition.getBeforeMethod() != null) {
            this.methodNode.addBeforeInterceptor(interceptorHolder, interceptorDefinition, apiId);
            this.declaringClass.setModified(true);
        } else {
            if (isDebug) {
                logger.debug("Skip adding before interceptorDefinition because the interceptorDefinition doesn't have before method: {}", interceptorClass.getName());
            }
        }

        // add after interface.
        if (isAfterInterceptor(captureType) && interceptorDefinition.getAfterMethod() != null) {
            this.methodNode.addAfterInterceptor(interceptorHolder, interceptorDefinition, apiId);
            this.declaringClass.setModified(true);
        } else {
            if (isDebug) {
                logger.debug("Skip adding after interceptor because the interceptor doesn't have after method: {}", interceptorClass.getName());
            }
        }
    }

    private boolean isBeforeInterceptor(CaptureType captureType) {
        return CaptureType.BEFORE == captureType || CaptureType.AROUND == captureType || CaptureType.BLOCK_AROUND == captureType;
    }

    private boolean isAfterInterceptor(CaptureType captureType) {
        return CaptureType.AFTER == captureType || CaptureType.AROUND == captureType || CaptureType.BLOCK_AROUND == captureType;
    }
}