/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.sdk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.sdk.AgentSdkAsyncConstants;

import java.util.Objects;

/**
 * TraceRunnable.asyncEntry(Runnable);
 * <p>TraceCallable.asyncEntry(Callable);
 */
public class AsyncEntryInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public AsyncEntryInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();

    }

    private boolean validate(final Object result) {
        if (!(result instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid result object. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
            }
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(this.descriptor);
            recorder.recordServiceType(AgentSdkAsyncConstants.AGENT_SDK_ASYNC);
            recorder.recordException(throwable);

            boolean r = validate(result);
            if (r) {
                // make asynchronous trace-id
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set asyncContext {}", asyncContext);
                }
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
