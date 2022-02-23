/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.CancellableContinuation;

/**
 * @author Taejin Koo
 */
public class DispatchInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final ServiceType serviceType;

    public DispatchInterceptor(TraceContext traceContext, MethodDescriptor descriptor, ServiceType serviceType) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
        this.serviceType = serviceType;
    }

    public DispatchInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this(traceContext, descriptor, ServiceType.INTERNAL_METHOD);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, descriptor.getClassName(), descriptor.getMethodName(), descriptor.getParameterDescriptor(), args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (isCompletedContinuation(args)) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(serviceType);
        AsyncContextAccessor accessor = ArrayArgumentUtils.getArgument(args, 0, AsyncContextAccessor.class);
        if (accessor != null) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            accessor._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    private boolean isCompletedContinuation(final Object[] args) {
        if (ArrayUtils.getLength(args) == 2) {
            Continuation continuation = ArrayArgumentUtils.getArgument(args, 1, Continuation.class);
            if (continuation instanceof CancellableContinuation) {
                return ((CancellableContinuation) continuation).isCompleted();
            }
        }
        return false;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, descriptor.getClassName(), descriptor.getMethodName(), descriptor.getParameterDescriptor(), args, result, throwable);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (isCompletedContinuation(args)) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            recorder.recordServiceType(serviceType);
        } finally {
            trace.traceBlockEnd();
        }

    }

}
