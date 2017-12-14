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
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorType;
import com.navercorp.pinpoint.profiler.interceptor.factory.AnnotatedInterceptorFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaehong.kim
 */
public class ASMMethod implements InstrumentMethod {
    // TODO fix inject InterceptorDefinitionFactory
    private static final InterceptorDefinitionFactory interceptorDefinitionFactory = new InterceptorDefinitionFactory();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ObjectBinderFactory objectBinderFactory;
    private final InstrumentContext pluginContext;
    private final InterceptorRegistryBinder interceptorRegistryBinder;
    private final ASMClass declaringClass;
    private final ASMMethodNodeAdapter methodNode;
    private final MethodDescriptor descriptor;
    private final ApiMetaDataService apiMetaDataService;

    // TODO fix inject ScopeFactory
    private static final ScopeFactory scopeFactory = new ScopeFactory();


    public ASMMethod(ObjectBinderFactory objectBinderFactory, InstrumentContext pluginContext, ApiMetaDataService apiMetaDataService, InterceptorRegistryBinder interceptorRegistryBinder, ASMClass declaringClass, MethodNode methodNode) {
        this(objectBinderFactory, pluginContext, interceptorRegistryBinder, apiMetaDataService, declaringClass, new ASMMethodNodeAdapter(JavaAssistUtils.javaNameToJvmName(declaringClass.getName()), methodNode));

    }

    public ASMMethod(ObjectBinderFactory objectBinderFactory, InstrumentContext pluginContext, InterceptorRegistryBinder interceptorRegistryBinder, ApiMetaDataService apiMetaDataService, ASMClass declaringClass, ASMMethodNodeAdapter methodNode) {
        if (objectBinderFactory == null) {
            throw new NullPointerException("objectBinderFactory must not be null");
        }
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext must not be null");
        }
        if (apiMetaDataService == null) {
            throw new NullPointerException("apiMetaDataService must not be null");
        }
        this.objectBinderFactory = objectBinderFactory;
        this.pluginContext = pluginContext;
        this.interceptorRegistryBinder = interceptorRegistryBinder;
        this.apiMetaDataService = apiMetaDataService;
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

    @Override
    public int addInterceptor(String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        return addInterceptor0(interceptorClassName, null, null, null);
    }

    @Override
    public int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        return addInterceptor0(interceptorClassName, constructorArgs, null, null);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClassName, null, interceptorScope, null);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        return addInterceptor0(interceptorClassName, null, scope, null);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClassName, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return addInterceptor0(interceptorClassName, null, scope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClassName, constructorArgs, interceptorScope, null);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        return addInterceptor0(interceptorClassName, constructorArgs, scope, null);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClassName, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return addInterceptor0(interceptorClassName, constructorArgs, scope, executionPolicy);
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
    int addInterceptorInternal(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        if (interceptorClassName == null) {
            throw new NullPointerException("interceptorClassName must not be null");
        }
        return addInterceptor0(interceptorClassName, constructorArgs, scope, executionPolicy);
    }

    private int addInterceptor0(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        final ClassLoader classLoader = this.declaringClass.getClassLoader();
        final ScopeInfo scopeInfo = scopeFactory.newScopeInfo(classLoader, pluginContext, interceptorClassName, scope, executionPolicy);
        final Interceptor interceptor = createInterceptor(classLoader, interceptorClassName, scopeInfo, constructorArgs);
        final int interceptorId = this.interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(interceptor);

        addInterceptor0(interceptor, interceptorId);
        return interceptorId;
    }

    private Interceptor createInterceptor(ClassLoader classLoader, String interceptorClassName, ScopeInfo scopeInfo, Object[] constructorArgs) {
        // exception handling.
        final AnnotatedInterceptorFactory factory = objectBinderFactory.newAnnotatedInterceptorFactory(this.pluginContext, true);
        final Interceptor interceptor = factory.getInterceptor(classLoader, interceptorClassName, constructorArgs, scopeInfo, this.declaringClass, this);

        return interceptor;
    }

    private void addInterceptor0(Interceptor interceptor, int interceptorId) {
        if (interceptor == null) {
            throw new NullPointerException("interceptor must not be null");
        }

        final InterceptorDefinition interceptorDefinition = this.interceptorDefinitionFactory.createInterceptorDefinition(interceptor.getClass());
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
            apiId = this.apiMetaDataService.cacheApi(this.descriptor);
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