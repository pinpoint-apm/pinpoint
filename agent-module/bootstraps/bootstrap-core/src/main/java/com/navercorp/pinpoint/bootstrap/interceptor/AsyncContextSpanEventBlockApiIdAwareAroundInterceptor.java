/*
 * Copyright 2024 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;

public abstract class AsyncContextSpanEventBlockApiIdAwareAroundInterceptor extends AbstractAsyncContextSpanEventInterceptor implements BlockApiIdAwareAroundInterceptor {

    public AsyncContextSpanEventBlockApiIdAwareAroundInterceptor(TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    public TraceBlock before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final AsyncContext asyncContext = getAsyncContext(target, args);
        if (asyncContext == null) {
            return null;
        }

        final Trace trace = getAsyncTrace(asyncContext);
        if (trace == null) {
            return null;
        }

        // entry scope.
        ScopeUtils.entryAsyncTraceScope(trace);

        try {
            // trace event for default & async.
            final TraceBlock traceBlock = trace.traceBlockBeginAndGet();
            beforeTrace(asyncContext, trace, traceBlock, target, apiId, args);
            doInBeforeTrace(traceBlock, asyncContext, target, apiId, args);
            return traceBlock;
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }

        return null;
    }

    protected void beforeTrace(final AsyncContext asyncContext, final Trace trace, final SpanEventRecorder recorder, final Object target, int apiId, final Object[] args) {
    }

    protected abstract void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args);

    @Override
    public void after(TraceBlock block, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final AsyncContext asyncContext = getAsyncContext(target, args, result, throwable);
        if (asyncContext == null) {
            return;
        }

        if (block == null) {
            return;
        }

        final Trace trace = block.getTrace();
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

        try (TraceBlock traceBlock = block) {
            afterTrace(asyncContext, trace, traceBlock, target, apiId, args, result, throwable);
            doInAfterTrace(traceBlock, target, apiId, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            if (ScopeUtils.isAsyncTraceEndScope(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
    }

    protected void afterTrace(final AsyncContext asyncContext, final Trace trace, final SpanEventRecorder recorder, final Object target, int apiId, final Object[] args, final Object result, final Throwable throwable) {
    }

    protected abstract void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable);
}