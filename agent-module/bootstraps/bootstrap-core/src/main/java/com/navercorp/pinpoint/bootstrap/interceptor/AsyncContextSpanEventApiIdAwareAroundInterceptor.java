/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;

public abstract class AsyncContextSpanEventApiIdAwareAroundInterceptor extends AbstractAsyncContextSpanEventInterceptor implements ApiIdAwareAroundInterceptor {
    public AsyncContextSpanEventApiIdAwareAroundInterceptor(TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    public void before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final AsyncContext asyncContext = getAsyncContext(target, args);
        if (asyncContext == null) {
            return;
        }

        final Trace trace = getAsyncTrace(asyncContext);
        if (trace == null) {
            return;
        }

        // entry scope.
        ScopeUtils.entryAsyncTraceScope(trace);

        try {
            // trace event for default & async.
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            beforeTrace(asyncContext, trace, recorder, target, apiId, args);
            doInBeforeTrace(recorder, asyncContext, target, apiId, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected void beforeTrace(final AsyncContext asyncContext, final Trace trace, final SpanEventRecorder recorder, final Object target, final int apiId, final Object[] args) {
    }

    protected abstract void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args);

    @Override
    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final AsyncContext asyncContext = getAsyncContext(target, args, result, throwable);
        if (asyncContext == null) {
            if (isTrace) {
                logger.trace("AsyncContext not found");
            }
            return;
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }

        // leave scope.
        if (!ScopeUtils.leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            // delete unstable trace.
            deleteAsyncContext(trace, asyncContext);
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            afterTrace(asyncContext, trace, recorder, target, apiId, args, result, throwable);
            doInAfterTrace(recorder, target, apiId, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (ScopeUtils.isAsyncTraceEndScope(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
    }

    protected void afterTrace(final AsyncContext asyncContext, final Trace trace, final SpanEventRecorder recorder, final Object target, int apiId, final Object[] args, final Object result, final Throwable throwable) {
    }

    protected abstract void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable);
}