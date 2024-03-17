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
import com.navercorp.pinpoint.common.util.ArrayUtils;
import kotlin.coroutines.Continuation;

/**
 * @author Taejin Koo
 */
public class ScheduleResumeInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final ServiceType serviceType;

    public ScheduleResumeInterceptor(TraceContext traceContext, MethodDescriptor descriptor, ServiceType serviceType) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
        this.serviceType = serviceType;
    }

    public ScheduleResumeInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this(traceContext, descriptor, ServiceType.INTERNAL_METHOD);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, descriptor.getClassName(), descriptor.getMethodName(), descriptor.getParameterDescriptor(), args);
        }

        final Continuation continuation = getContinuation(args);
        if (continuation == null) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        final AsyncContextAccessor asyncContextAccessor = getAsyncContextAccessor(continuation);
        if (asyncContextAccessor != null) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    private Continuation getContinuation(final Object[] args) {
        if (ArrayUtils.getLength(args) == 2 && args[1] instanceof Continuation) {
            return (Continuation) args[1];
        }
        return null;
    }

    private AsyncContextAccessor getAsyncContextAccessor(final Continuation continuation) {
        if (continuation == null) {
            return null;
        }

        Object object = continuation.getContext();
        if (object instanceof AsyncContextAccessor) {
            return (AsyncContextAccessor) object;
        }
        return null;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, descriptor.getClassName(), descriptor.getMethodName(), descriptor.getParameterDescriptor(), args, result, throwable);
        }

        final Continuation continuation = getContinuation(args);
        if (continuation == null) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordServiceType(serviceType);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

}