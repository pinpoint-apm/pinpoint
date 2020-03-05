/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.AsyncStateSupport;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.method.AsyncListenerOnCompleteMethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.method.AsyncListenerOnErrorMethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.method.AsyncListenerOnTimeoutMethodDescriptor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public class AsyncListenerInterceptorHelper {
    private static final MethodDescriptor ASYNC_LISTENER_ON_COMPLETE_METHOD_DESCRIPTOR = new AsyncListenerOnCompleteMethodDescriptor();
    private static final MethodDescriptor ASYNC_LISTENER_ON_ERROR_METHOD_DESCRIPTOR = new AsyncListenerOnErrorMethodDescriptor();
    private static final MethodDescriptor ASYNC_LISTENER_ON_TIMEOUT_METHOD_DESCRIPTOR = new AsyncListenerOnTimeoutMethodDescriptor();

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final AsyncContext asyncContext;
    private final HttpStatusCodeRecorder httpStatusCodeRecorder;

    public AsyncListenerInterceptorHelper(final TraceContext traceContext, final AsyncContext asyncContext) {
        Assert.requireNonNull(traceContext, "traceContext");
        this.asyncContext = Assert.requireNonNull(asyncContext, "asyncContext");
        this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(traceContext.getProfilerConfig().getHttpStatusCodeErrors());

        traceContext.cacheApi(ASYNC_LISTENER_ON_COMPLETE_METHOD_DESCRIPTOR);
        traceContext.cacheApi(ASYNC_LISTENER_ON_ERROR_METHOD_DESCRIPTOR);
        traceContext.cacheApi(ASYNC_LISTENER_ON_TIMEOUT_METHOD_DESCRIPTOR);
    }

    public void complete(Throwable throwable, int statusCode) {
        if (isDebug) {
            logger.debug("Complete async listener. throwable={}, statusCode={}", throwable, statusCode);
        }

        final Trace trace = this.asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            return;
        }

        try {
            // Record http status code
            recordHttpStatusCode(trace, statusCode);
            // Record event
            recordAsyncEvent(trace, throwable, ASYNC_LISTENER_ON_COMPLETE_METHOD_DESCRIPTOR);
        } finally {
            // Close async trace
            close(trace);
            // End Point
            finish();
        }
    }

    private void recordHttpStatusCode(final Trace trace, final int statusCode) {
        // Record http status code
        final SpanRecorder spanRecorder = trace.getSpanRecorder();
        this.httpStatusCodeRecorder.record(spanRecorder, statusCode);
    }

    public void error(Throwable throwable) {
        if (isDebug) {
            logger.debug("Error async listener. throwable={}", throwable);
        }

        final Trace trace = this.asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            return;
        }

        try {
            recordAsyncEvent(trace, throwable, ASYNC_LISTENER_ON_ERROR_METHOD_DESCRIPTOR);
        } finally {
            close(trace);
        }
    }

    public void timeout(Throwable throwable) {
        if (isDebug) {
            logger.debug("Timeout async listener. throwable={}", throwable);
        }

        final Trace trace = this.asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            return;
        }

        try {
            recordAsyncEvent(trace, throwable, ASYNC_LISTENER_ON_TIMEOUT_METHOD_DESCRIPTOR);
        } finally {
            close(trace);
        }
    }

    private void recordAsyncEvent(final Trace trace, final Throwable throwable, MethodDescriptor methodDescriptor) {
        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(ServiceType.SERVLET);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        trace.traceBlockEnd();
    }

    private void close(final Trace trace) {
        trace.close();
        this.asyncContext.close();
    }

    private void finish() {
        if (this.asyncContext instanceof AsyncStateSupport) {
            final AsyncStateSupport asyncStateSupport = (AsyncStateSupport) this.asyncContext;
            AsyncState asyncState = asyncStateSupport.getAsyncState();
            asyncState.finish();
        }
    }
}