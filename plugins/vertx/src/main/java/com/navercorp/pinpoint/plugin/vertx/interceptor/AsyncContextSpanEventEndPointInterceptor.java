/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextUtils;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public abstract class AsyncContextSpanEventEndPointInterceptor implements AroundInterceptor {

    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();
    protected static final String ASYNC_TRACE_SCOPE = AsyncContext.ASYNC_TRACE_SCOPE;

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;

    public AsyncContextSpanEventEndPointInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");

    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final AsyncContext asyncContext = getAsyncContext(target);
        if (asyncContext == null) {
            logger.debug("AsyncContext not found");
            return;
        }

        final Trace trace = getAsyncTrace(asyncContext);
        if (trace == null) {
            return;
        }

        if (isDebug) {
            logger.debug("Asynchronous invocation. asyncTraceId={}, trace={}", asyncContext, trace);
        }
        // entry scope.
        ScopeUtils.entryAsyncTraceScope(trace);

        try {
            // trace event for default & async.
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            doInBeforeTrace(recorder, asyncContext, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected abstract void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final AsyncContext asyncContext = getAsyncContext(target);
        if (asyncContext == null) {
            logger.debug("Not found asynchronous invocation metadata");
            return;
        }
        if (isDebug) {
            logger.debug("Asynchronous invocation. asyncContext={}", asyncContext);
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }
        if (isDebug) {
            logger.debug("Asynchronous invocation. asyncTraceId={}, trace={}", asyncContext, trace);
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

        prepareAfter(trace, target, args, result, throwable);
        if (!trace.canSampled()) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (ScopeUtils.isAsyncTraceEndScope(trace)) {
                if (isDebug) {
                    logger.debug("Arrived at async trace destination. asyncTraceId={}", asyncContext);
                }
                deleteAsyncTrace(trace);
            }
            finishAsyncState(asyncContext);
        }
    }

    protected void prepareAfter(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
    }

    protected abstract void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable);

    protected AsyncContext getAsyncContext(Object target) {
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    private Trace getAsyncTrace(AsyncContext asyncContext) {
        final Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            if (isDebug) {
                logger.debug("Failed to continue async trace. 'result is null'");
            }
            return null;
        }
        if (isDebug) {
            logger.debug("getAsyncTrace() trace {}, asyncContext={}", trace, asyncContext);
        }

        return trace;
    }

    private void deleteAsyncTrace(final Trace trace) {
        if (isDebug) {
            logger.debug("Delete async trace {}.", trace);
        }
        traceContext.removeTraceObject();
        trace.close();
    }

    private void finishAsyncState(final AsyncContext asyncContext) {
        if (AsyncContextUtils.asyncStateFinish(asyncContext)) {
            if (isDebug) {
                logger.debug("finished asyncState. asyncTraceId={}", asyncContext);
            }
        }
    }

}
