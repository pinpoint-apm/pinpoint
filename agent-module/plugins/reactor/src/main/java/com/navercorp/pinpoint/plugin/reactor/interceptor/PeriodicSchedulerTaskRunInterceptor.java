/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * Records one independent root transaction for each periodic scheduler execution.
 * <p>
 * The transform deliberately injects neither an {@code AsyncContext} field nor a constructor
 * interceptor. Consequently, a long-lived periodic task cannot retain the transaction that
 * registered it. If execution starts while another trace is bound to the worker thread, that trace
 * is temporarily detached without closing it and restored after the periodic transaction ends.
 * <p>
 * This interceptor must be installed with a shared {@code BOUNDARY} scope on both {@code run()}
 * and {@code call()}; Reactor versions where one delegates to the other then create only one root.
 */
public class PeriodicSchedulerTaskRunInterceptor implements AroundInterceptor {
    private static final String LOCAL = "LOCAL";
    private static final String PERIODIC_TASK_RPC = "Reactor periodic scheduler task";

    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final ServiceType serviceType;
    private final InterceptorScope scope;

    public PeriodicSchedulerTaskRunInterceptor(TraceContext traceContext,
                                               MethodDescriptor methodDescriptor,
                                               ServiceType serviceType,
                                               InterceptorScope scope) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.scope = Objects.requireNonNull(scope, "scope");
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final InterceptorScopeInvocation invocation = scope.getCurrentInvocation();
        final ExecutionState state = new ExecutionState();
        invocation.setAttachment(state);

        try {
            final Trace current = traceContext.currentRawTraceObject();
            if (current != null) {
                // false is essential: removeTraceObject() closes an unsampled trace. This trace is
                // only suspended and remains owned by the caller of the periodic task.
                state.suspendedTrace = traceContext.removeTraceObject(false);
            }

            final Trace trace = traceContext.newTraceObject();
            state.periodicTrace = trace;
            if (trace == null) {
                cleanup(state);
                invocation.removeAttachment();
                return;
            }

            if (trace.canSampled()) {
                final SpanRecorder recorder = trace.getSpanRecorder();
                recorder.recordServiceType(serviceType);
                recorder.recordApi(methodDescriptor);
                recorder.recordRpcName(PERIODIC_TASK_RPC);
                recorder.recordEndPoint(LOCAL);
                recorder.recordRemoteAddress(LOCAL);
            }
        } catch (Throwable th) {
            cleanup(state);
            invocation.removeAttachment();
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to start an independent periodic scheduler transaction. Cause:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final InterceptorScopeInvocation invocation = scope.getCurrentInvocation();
        final Object attachment = invocation.getAttachment();
        if (!(attachment instanceof ExecutionState)) {
            return;
        }

        final ExecutionState state = (ExecutionState) attachment;
        invocation.removeAttachment();
        try {
            final Trace trace = state.periodicTrace;
            if (trace != null && trace.canSampled()) {
                trace.getSpanRecorder().recordException(throwable);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to finish an independent periodic scheduler transaction. Cause:{}", th.getMessage(), th);
            }
        } finally {
            cleanup(state);
        }
    }

    private void cleanup(ExecutionState state) {
        if (state.periodicTrace != null) {
            try {
                traceContext.removeTraceObject(false);
            } catch (Throwable th) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to unbind an independent periodic scheduler transaction. Cause:{}", th.getMessage(), th);
                }
            }

            try {
                if (!state.periodicTrace.isClosed()) {
                    state.periodicTrace.close();
                }
            } catch (Throwable th) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to close an independent periodic scheduler transaction. Cause:{}", th.getMessage(), th);
                }
            }
        }
        restoreSuspendedTrace(state);
    }

    private void restoreSuspendedTrace(ExecutionState state) {
        if (state.suspendedTrace == null) {
            return;
        }
        try {
            traceContext.continueTraceObject(state.suspendedTrace);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to restore the trace suspended by a periodic scheduler transaction. Cause:{}", th.getMessage(), th);
            }
        }
    }

    private static class ExecutionState {
        private Trace suspendedTrace;
        private Trace periodicTrace;
    }
}
