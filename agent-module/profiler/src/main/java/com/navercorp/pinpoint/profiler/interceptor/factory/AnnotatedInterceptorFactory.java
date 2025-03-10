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
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleAroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockAroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockAroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockAroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockAroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockAroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockAroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleBlockStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandleStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedBlockStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedBlockStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetricRegistry;
import com.navercorp.pinpoint.profiler.instrument.ScopeInfo;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.InterceptorArgumentProvider;

import java.util.Objects;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class AnnotatedInterceptorFactory implements InterceptorFactory {
    private final ProfilerConfig profilerConfig;
    private final TraceContext traceContext;
    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;
    private final CustomMetricRegistry customMetricRegistry;
    private final ApiMetaDataService apiMetaDataService;

    private final InstrumentContext pluginContext;
    private final ExceptionHandlerFactory exceptionHandlerFactory;
    private final RequestRecorderFactory requestRecorderFactory;

    public AnnotatedInterceptorFactory(ProfilerConfig profilerConfig,
                                       TraceContext traceContext,
                                       DataSourceMonitorRegistry dataSourceMonitorRegistry,
                                       CustomMetricRegistry customMetricRegistry,
                                       ApiMetaDataService apiMetaDataService,
                                       InstrumentContext pluginContext,
                                       ExceptionHandlerFactory exceptionHandlerFactory,
                                       RequestRecorderFactory requestRecorderFactory) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.dataSourceMonitorRegistry = Objects.requireNonNull(dataSourceMonitorRegistry, "dataSourceMonitorRegistry");
        this.customMetricRegistry = Objects.requireNonNull(customMetricRegistry, "customMetricRegistry");
        this.apiMetaDataService = Objects.requireNonNull(apiMetaDataService, "apiMetaDataService");

        this.pluginContext = Objects.requireNonNull(pluginContext, "pluginContext");
        this.exceptionHandlerFactory = Objects.requireNonNull(exceptionHandlerFactory, "exceptionHandlerFactory");
        this.requestRecorderFactory = Objects.requireNonNull(requestRecorderFactory, "requestRecorderFactory");
    }

    @Override
    public Interceptor newInterceptor(Class<?> interceptorClass, Object[] providedArguments, ScopeInfo scopeInfo, MethodDescriptor methodDescriptor) {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeInfo, "scopeInfo");

        final InterceptorScope interceptorScope = scopeInfo.getInterceptorScope();
        InterceptorArgumentProvider interceptorArgumentProvider = new InterceptorArgumentProvider(dataSourceMonitorRegistry, customMetricRegistry, apiMetaDataService, requestRecorderFactory, interceptorScope, methodDescriptor);
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
        } else if (interceptor instanceof BlockAroundInterceptor) {
            return new ScopedBlockInterceptor((BlockAroundInterceptor) interceptor, scope, policy);
        } else if (interceptor instanceof BlockStaticAroundInterceptor) {
            return new ScopedBlockStaticAroundInterceptor((BlockStaticAroundInterceptor) interceptor, scope, policy);
        } else if (interceptor instanceof BlockAroundInterceptor5) {
            return new ScopedBlockInterceptor5((BlockAroundInterceptor5) interceptor, scope, policy);
        } else if (interceptor instanceof BlockAroundInterceptor4) {
            return new ScopedBlockInterceptor4((BlockAroundInterceptor4) interceptor, scope, policy);
        } else if (interceptor instanceof BlockAroundInterceptor3) {
            return new ScopedBlockInterceptor3((BlockAroundInterceptor3) interceptor, scope, policy);
        } else if (interceptor instanceof BlockAroundInterceptor2) {
            return new ScopedBlockInterceptor2((BlockAroundInterceptor2) interceptor, scope, policy);
        } else if (interceptor instanceof BlockAroundInterceptor1) {
            return new ScopedBlockInterceptor1((BlockAroundInterceptor1) interceptor, scope, policy);
        } else if (interceptor instanceof BlockAroundInterceptor0) {
            return new ScopedBlockInterceptor0((BlockAroundInterceptor0) interceptor, scope, policy);
        } else if (interceptor instanceof BlockApiIdAwareAroundInterceptor) {
            return new ScopedBlockApiIdAwareAroundInterceptor((BlockApiIdAwareAroundInterceptor) interceptor, scope, policy);
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
        } else if (interceptor instanceof BlockAroundInterceptor) {
            return new ExceptionHandleScopedBlockInterceptor((BlockAroundInterceptor) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof BlockStaticAroundInterceptor) {
            return new ExceptionHandleScopedBlockStaticAroundInterceptor((BlockStaticAroundInterceptor) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor5) {
            return new ExceptionHandleScopedBlockInterceptor5((BlockAroundInterceptor5) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor4) {
            return new ExceptionHandleScopedBlockInterceptor4((BlockAroundInterceptor4) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor3) {
            return new ExceptionHandleScopedBlockInterceptor3((BlockAroundInterceptor3) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor2) {
            return new ExceptionHandleScopedBlockInterceptor2((BlockAroundInterceptor2) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor1) {
            return new ExceptionHandleScopedBlockInterceptor1((BlockAroundInterceptor1) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor0) {
            return new ExceptionHandleScopedBlockInterceptor0((BlockAroundInterceptor0) interceptor, scope, policy, exceptionHandler);
        } else if (interceptor instanceof BlockApiIdAwareAroundInterceptor) {
            return new ExceptionHandleScopedBlockApiIdAwareAroundInterceptor((BlockApiIdAwareAroundInterceptor) interceptor, scope, policy, exceptionHandler);
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
        } else if (interceptor instanceof BlockAroundInterceptor) {
            return new ExceptionHandleBlockAroundInterceptor((BlockAroundInterceptor) interceptor, exceptionHandler);
        } else if (interceptor instanceof BlockStaticAroundInterceptor) {
            return new ExceptionHandleBlockStaticAroundInterceptor((BlockStaticAroundInterceptor) interceptor, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor5) {
            return new ExceptionHandleBlockAroundInterceptor5((BlockAroundInterceptor5) interceptor, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor4) {
            return new ExceptionHandleBlockAroundInterceptor4((BlockAroundInterceptor4) interceptor, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor3) {
            return new ExceptionHandleBlockAroundInterceptor3((BlockAroundInterceptor3) interceptor, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor2) {
            return new ExceptionHandleBlockAroundInterceptor2((BlockAroundInterceptor2) interceptor, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor1) {
            return new ExceptionHandleBlockAroundInterceptor1((BlockAroundInterceptor1) interceptor, exceptionHandler);
        } else if (interceptor instanceof BlockAroundInterceptor0) {
            return new ExceptionHandleBlockAroundInterceptor0((BlockAroundInterceptor0) interceptor, exceptionHandler);
        } else if (interceptor instanceof BlockApiIdAwareAroundInterceptor) {
            return new ExceptionHandleBlockApiIdAwareAroundInterceptor((BlockApiIdAwareAroundInterceptor) interceptor, exceptionHandler);
        }

        throw new IllegalArgumentException("Unexpected interceptor type: " + interceptor.getClass());
    }
}