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

package com.navercorp.pinpoint.profiler.interceptor.factory;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.ScopeInfo;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.InterceptorArgumentProvider;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class AnnotatedInterceptorFactory implements InterceptorFactory {
    private final ProfilerConfig profilerConfig;
    private final TraceContext traceContext;
    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;
    private final ApiMetaDataService apiMetaDataService;
    private final InstrumentContext pluginContext;
    private final ExceptionHandlerFactory exceptionHandlerFactory;
    private final RequestRecorderFactory requestRecorderFactory;

    public AnnotatedInterceptorFactory(ProfilerConfig profilerConfig,
                                       TraceContext traceContext,
                                       DataSourceMonitorRegistry dataSourceMonitorRegistry,
                                       ApiMetaDataService apiMetaDataService,
                                       InstrumentContext pluginContext,
                                       ExceptionHandlerFactory exceptionHandlerFactory,
                                       RequestRecorderFactory requestRecorderFactory) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.traceContext = Assert.requireNonNull(traceContext, "traceContext");
        this.dataSourceMonitorRegistry = Assert.requireNonNull(dataSourceMonitorRegistry, "dataSourceMonitorRegistry");
        this.apiMetaDataService = Assert.requireNonNull(apiMetaDataService, "apiMetaDataService");
        this.pluginContext = Assert.requireNonNull(pluginContext, "pluginContext");
        this.exceptionHandlerFactory = Assert.requireNonNull(exceptionHandlerFactory, "exceptionHandlerFactory");
        this.requestRecorderFactory = Assert.requireNonNull(requestRecorderFactory, "requestRecorderFactory");
    }

    @Override
    public Interceptor newInterceptor(Class<?> interceptorClass, Object[] providedArguments, ScopeInfo scopeInfo, InstrumentClass target, InstrumentMethod targetMethod) {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeInfo, "scopeInfo");

        final InterceptorScope interceptorScope = scopeInfo.getInterceptorScope();
        InterceptorArgumentProvider interceptorArgumentProvider = new InterceptorArgumentProvider(dataSourceMonitorRegistry, apiMetaDataService, requestRecorderFactory, interceptorScope, target, targetMethod);

        AutoBindingObjectFactory factory = new AutoBindingObjectFactory(profilerConfig, traceContext, pluginContext, interceptorClass.getClassLoader());
        Interceptor interceptor = (Interceptor) factory.createInstance(interceptorClass, providedArguments, interceptorArgumentProvider);

        return wrap(interceptor, scopeInfo, interceptorScope);
    }

    private Interceptor wrap(Interceptor interceptor, ScopeInfo scopeInfo, InterceptorScope interceptorScope) {
        if (interceptorScope != null) {
            final ExecutionPolicy executionPolicy = getExecutionPolicy(scopeInfo.getExecutionPolicy());
            if (exceptionHandlerFactory.isHandleException()) {
                return wrapByExceptionHandleScope(interceptor, interceptorScope, executionPolicy);
            } else {
                return wrapByScope(interceptor, interceptorScope, executionPolicy);
            }
        } else {
            if (exceptionHandlerFactory.isHandleException()) {
                return wrapByExceptionHandle(interceptor);
            }
        }
        return interceptor;
    }

    private ExecutionPolicy getExecutionPolicy(ExecutionPolicy policy) {
        if (policy == null) {
            return ExecutionPolicy.BOUNDARY;
        }
        return policy;
    }

    private Interceptor wrapByScope(Interceptor interceptor, InterceptorScope scope, ExecutionPolicy policy) {
        if (interceptor instanceof AroundInterceptor) {
            return new ScopedInterceptor((AroundInterceptor) interceptor, scope, policy);
        } else if (interceptor instanceof StaticAroundInterceptor) {
            return new ScopedStaticAroundInterceptor((StaticAroundInterceptor) interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor5) {
            return new ScopedInterceptor5((AroundInterceptor5) interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor4) {
            return new ScopedInterceptor4((AroundInterceptor4) interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor3) {
            return new ScopedInterceptor3((AroundInterceptor3) interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor2) {
            return new ScopedInterceptor2((AroundInterceptor2) interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor1) {
            return new ScopedInterceptor1((AroundInterceptor1) interceptor, scope, policy);
        } else if (interceptor instanceof AroundInterceptor0) {
            return new ScopedInterceptor0((AroundInterceptor0) interceptor, scope, policy);
        } else if (interceptor instanceof ApiIdAwareAroundInterceptor) {
            return new ScopedApiIdAwareAroundInterceptor((ApiIdAwareAroundInterceptor) interceptor, scope, policy);
        }

        throw new IllegalArgumentException("Unexpected interceptor type: " + interceptor.getClass());
    }

    private Interceptor wrapByExceptionHandleScope(Interceptor interceptor, InterceptorScope scope, ExecutionPolicy policy) {
        final ExceptionHandler exceptionHandler = exceptionHandlerFactory.getExceptionHandler();
        if (interceptor instanceof AroundInterceptor) {
            return new ExceptionHandleScopedInterceptor((AroundInterceptor) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof StaticAroundInterceptor) {
            return new ExceptionHandleScopedStaticAroundInterceptor((StaticAroundInterceptor) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor5) {
            return new ExceptionHandleScopedInterceptor5((AroundInterceptor5) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor4) {
            return new ExceptionHandleScopedInterceptor4((AroundInterceptor4) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor3) {
            return new ExceptionHandleScopedInterceptor3((AroundInterceptor3) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor2) {
            return new ExceptionHandleScopedInterceptor2((AroundInterceptor2) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor1) {
            return new ExceptionHandleScopedInterceptor1((AroundInterceptor1) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor0) {
            return new ExceptionHandleScopedInterceptor0((AroundInterceptor0) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof ApiIdAwareAroundInterceptor) {
            return new ExceptionHandleScopedApiIdAwareAroundInterceptor((ApiIdAwareAroundInterceptor) interceptor, scope, policy, exceptionHandler);
        }

        throw new IllegalArgumentException("Unexpected interceptor type: " + interceptor.getClass());
    }

    private Interceptor wrapByExceptionHandle(Interceptor interceptor) {
        final ExceptionHandler exceptionHandler = exceptionHandlerFactory.getExceptionHandler();
        if (interceptor instanceof AroundInterceptor) {
            return new ExceptionHandleAroundInterceptor((AroundInterceptor) interceptor, exceptionHandler);
        } else if (interceptor instanceof StaticAroundInterceptor) {
            return new ExceptionHandleStaticAroundInterceptor((StaticAroundInterceptor) interceptor, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor5) {
            return new ExceptionHandleAroundInterceptor5((AroundInterceptor5) interceptor, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor4) {
            return new ExceptionHandleAroundInterceptor4((AroundInterceptor4) interceptor, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor3) {
            return new ExceptionHandleAroundInterceptor3((AroundInterceptor3) interceptor, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor2) {
            return new ExceptionHandleAroundInterceptor2((AroundInterceptor2) interceptor, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor1) {
            return new ExceptionHandleAroundInterceptor1((AroundInterceptor1) interceptor, exceptionHandler);
        } else if (interceptor instanceof AroundInterceptor0) {
            return new ExceptionHandleAroundInterceptor0((AroundInterceptor0) interceptor, exceptionHandler);
        } else if (interceptor instanceof ApiIdAwareAroundInterceptor) {
            return new ExceptionHandleApiIdAwareAroundInterceptor((ApiIdAwareAroundInterceptor) interceptor, exceptionHandler);
        }

        throw new IllegalArgumentException("Unexpected interceptor type: " + interceptor.getClass());
    }
}