/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;

import java.util.Objects;

/**
 * Async span-event interceptor base whose {@code before()} opens the trace block under the
 * {@link AsyncContext} it looked up and hands that block, carrying the context, to {@code after()}.
 * {@code after()} never looks the context up again. The hooks and constructors match
 * {@link AsyncContextSpanEventSimpleAroundInterceptor}, minus the after-side
 * {@code getAsyncContext(target, args, result, throwable)} override, so an interceptor moves here by
 * changing its superclass and deleting that override.
 */
public abstract class AsyncContextTraceBlockSimpleAroundInterceptor extends AbstractAsyncContextTraceBlockInterceptor implements BlockAroundInterceptor {

    protected final MethodDescriptor methodDescriptor;

    public AsyncContextTraceBlockSimpleAroundInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this(traceContext, methodDescriptor, true);
    }

    public AsyncContextTraceBlockSimpleAroundInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, boolean asyncTraceBlock) {
        super(traceContext, asyncTraceBlock);
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

        // entry scope.
        ScopeUtils.entryAsyncTraceScope(trace);

        // the block carries the context to after().
        final TraceBlock traceBlock = trace.getTraceBlock(asyncContext);
        try {
            if (asyncTraceBlock && checkBeforeTraceBlockBegin(asyncContext, trace, target, args)) {
                traceBlock.begin();
                beforeTrace(asyncContext, trace, traceBlock, target, args);
                doInBeforeTrace(traceBlock, asyncContext, target, args);
            }
            beforeAction(asyncContext, trace, target, args);
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

    protected void beforeAction(AsyncContext asyncContext, Trace trace, Object target, Object[] args) {
    }

    @Override
    public void after(TraceBlock block, Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (block == null) {
            // before() did not open a block: nothing to balance.
            return;
        }

        final Trace trace = block.getTrace();
        if (trace == null) {
            return;
        }

        // the context before() opened the block with; null only when the Trace implementation
        // could not carry it, in which case the block is still closed and the scope still left
        // below, only the context-bound hooks and the AsyncContext release are skipped.
        final AsyncContext asyncContext = asyncContextOf(block);

        // leave scope. A scope that cannot be left (its depth is already 0: something else left it
        // for us) is treated as ended below; the block before() opened is still closed in order,
        // so the trace keeps what it recorded instead of being discarded with a frame left on it.
        final boolean scopeLeft = ScopeUtils.leaveAsyncTraceScope(trace);
        if (!scopeLeft && logger.isWarnEnabled()) {
            logger.warn("Failed to leave scope of async trace; closing it after the block. interceptor={}, trace={}", getClass().getName(), trace);
        }

        try (TraceBlock traceBlock = block) {
            if (asyncContext == null) {
                // the block carried no context (a Trace without getTraceBlock(AsyncContext) support):
                // skip the context-bound hooks, the block and the scope are still closed below.
                return;
            }
            if (asyncTraceBlock && traceBlock.isBegin()) {
                afterTrace(asyncContext, trace, traceBlock, target, args, result, throwable);
                doInAfterTrace(traceBlock, target, args, result, throwable);
            }
            afterAction(asyncContext, trace, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            if (!scopeLeft || ScopeUtils.isAsyncTraceEndScope(trace)) {
                closeAsyncTrace(trace, asyncContext);
            }
        }
    }

    protected void afterTrace(final AsyncContext asyncContext, final Trace trace, final SpanEventRecorder recorder, final Object target, final Object[] args, final Object result, final Throwable throwable) {
    }

    protected abstract void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable);

    protected void afterAction(AsyncContext asyncContext, Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
    }
}
