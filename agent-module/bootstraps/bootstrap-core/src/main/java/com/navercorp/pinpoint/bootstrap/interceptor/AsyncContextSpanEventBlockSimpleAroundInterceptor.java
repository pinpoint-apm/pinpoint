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
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;

import java.util.Objects;

public abstract class AsyncContextSpanEventBlockSimpleAroundInterceptor extends AbstractAsyncContextSpanEventInterceptor implements BlockAroundInterceptor {

    protected final MethodDescriptor methodDescriptor;

    public AsyncContextSpanEventBlockSimpleAroundInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext);
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
    }

    @Override
    public TraceBlock before(Object target, Object[] args) {
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

        ScopeUtils.entryAsyncTraceScope(trace);

        final TraceBlock traceBlock = trace.getTraceBlock();
        try {
            if (checkBeforeTraceBlockBegin(asyncContext, trace, target, args)) {
                traceBlock.begin();
                beforeTrace(asyncContext, trace, traceBlock, target, args);
                doInBeforeTrace(traceBlock, asyncContext, target, args);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }

        return traceBlock;
    }

    protected boolean checkBeforeTraceBlockBegin(AsyncContext asyncContext, Trace trace, Object target, Object[] args) {
        return true;
    }

    protected void beforeTrace(final AsyncContext asyncContext, final Trace trace, final SpanEventRecorder recorder, final Object target, final Object[] args) {
    }

    protected abstract void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args);

    @Override
    public void after(TraceBlock block, Object target, Object[] args, Object result, Throwable throwable) {
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
            if (traceBlock.isBegin()) {
                afterTrace(asyncContext, trace, traceBlock, target, args, result, throwable);
                doInAfterTrace(traceBlock, target, args, result, throwable);
            }
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

    protected void afterTrace(final AsyncContext asyncContext, final Trace trace, final SpanEventRecorder recorder, final Object target, final Object[] args, final Object result, final Throwable throwable) {
    }

    protected abstract void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable);
}