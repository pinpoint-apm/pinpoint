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
import com.navercorp.pinpoint.profiler.context.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.instrument.interceptor.CaptureType;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinition;
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

        final int interceptorId = newInterceptor(interceptorClass, null, null, null);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");

        final int interceptorId = newInterceptor(interceptorClass, constructorArgs, null, null);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final int interceptor = newInterceptor(interceptorClass, null, interceptorScope, null);
        addInterceptor0(interceptorClass, interceptor);
        return interceptor;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(interceptorScope, "interceptorScope");

        final int interceptorId = newInterceptor(interceptorClass, null, interceptorScope, null);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");
        Objects.requireNonNull(executionPolicy, "executionPolicy");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final int interceptorId = newInterceptor(interceptorClass, null, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        Objects.requireNonNull(executionPolicy, "executionPolicy");

        final int interceptorId = newInterceptor(interceptorClass, null, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(scopeName, "scopeName");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final int interceptorId = newInterceptor(interceptorClass, constructorArgs, interceptorScope, null);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(interceptorScope, "interceptorScope");

        final int interceptorId = newInterceptor(interceptorClass, constructorArgs, interceptorScope, null);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(scopeName, "scopeName");
        Objects.requireNonNull(executionPolicy, "executionPolicy");

        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        final int interceptorId = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        Objects.requireNonNull(executionPolicy, "executionPolicy");

        final int interceptorId = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    @Override
    public void addInterceptor(int interceptorId) throws InstrumentException {
        if (InterceptorRegistry.isInterceptorHolderEnable()) {
            final ASMInterceptorHolder holder = new ASMInterceptorHolder(interceptorId);
            final Class<? extends Interceptor> interceptorClass = holder.loadInterceptorClass(this.declaringClass.getClassLoader());
            addInterceptor0(interceptorClass, interceptorId);
        } else {
            final Interceptor interceptor = InterceptorRegistry.getInterceptor(interceptorId);
            if (interceptor == null) {
                // defense code
                throw new InstrumentException("not found interceptor " + interceptorId + " to " + this.methodNode.getLongName());
            }
            try {
                addInterceptor0(interceptor.getClass(), interceptorId);
            } catch (Exception e) {
                throw new InstrumentException("add interceptor0 " + interceptor.getClass().getName() + " to " + this.methodNode.getLongName(), e);
            }
        }
    }

    // for internal api
    int addInterceptorInternal(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");

        final int interceptorId = newInterceptor(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
        addInterceptor0(interceptorClass, interceptorId);
        return interceptorId;
    }

    private int newInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        final ScopeFactory scopeFactory = this.engineComponent.getScopeFactory();
        final ScopeInfo scopeInfo = scopeFactory.newScopeInfo(pluginContext, interceptorClass, interceptorScope, executionPolicy);
        return createInterceptor(interceptorClass, constructorArgs, scopeInfo);
    }

    private int createInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, ScopeInfo scopeInfo) throws InstrumentException {
        final ObjectBinderFactory objectBinderFactory = this.engineComponent.getObjectBinderFactory();
        final AnnotatedInterceptorFactory factory = objectBinderFactory.newAnnotatedInterceptorFactory(this.pluginContext);
        final ClassLoader classLoader = this.declaringClass.getClassLoader();
        int interceptorId = 0;
        if (classLoader == null || Boolean.FALSE == InterceptorRegistry.isInterceptorHolderEnable()) {
            // Bootstrap ClassLoader or disable interceptorHolder
            final Interceptor interceptor = factory.newInterceptor(interceptorClass, constructorArgs, scopeInfo, this.descriptor);
            interceptorId = this.engineComponent.addInterceptor(interceptor);
        } else {
            // InterceptorHolder
            interceptorId = this.engineComponent.addInterceptor();
            ASMInterceptorHolder.create(interceptorId, this.declaringClass.getClassLoader(), factory, interceptorClass, constructorArgs, scopeInfo, this.descriptor);
        }
        return interceptorId;
    }

    private void addInterceptor0(Class<? extends Interceptor> interceptorClass, int interceptorId) {
        Objects.requireNonNull(interceptorClass, "interceptorClass");

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
}