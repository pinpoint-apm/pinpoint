/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;

/**
 * @author jaehong.kim
 */
public abstract class AsyncContextSpanEventEndPointApiAwareInterceptor extends AbstractAsyncContextSpanEventEndPointInterceptor implements ApiIdAwareAroundInterceptor {

    public AsyncContextSpanEventEndPointApiAwareInterceptor(TraceContext traceContext) {
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

        if (isDebug) {
            logger.debug("Asynchronous invocation. asyncContext={}, trace={}", asyncContext, trace);
        }

        // entry scope.
        ScopeUtils.entryAsyncTraceScope(trace);

        try {
            // trace event for default & async.
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            beforeTrace(asyncContext, trace, recorder, target, apiId, args);
            doInBeforeTrace(recorder, target, apiId, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected void beforeTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
    }

    protected abstract void doInBeforeTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args);

    @Override
    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final AsyncContext asyncContext = getAsyncContext(target, args, result, throwable);
        if (asyncContext == null) {
            return;
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }
        if (isDebug) {
            logger.debug("Asynchronous invocation. asyncContext={}, trace={}", asyncContext, trace);
        }

        // leave scope.
        if (!ScopeUtils.leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            // delete unstable trace.
            deleteAsyncTrace(trace);
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
                deleteAsyncTrace(trace);
            }
            finishAsyncState(asyncContext);
        }
    }

    protected void afterTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }

    protected abstract void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable);
}