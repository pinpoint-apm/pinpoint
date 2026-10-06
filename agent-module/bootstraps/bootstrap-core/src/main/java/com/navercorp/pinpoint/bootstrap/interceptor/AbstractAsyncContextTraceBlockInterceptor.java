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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextTraceBlock;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

import java.util.Objects;

/**
 * Root of the async interceptor bases that carry the {@link AsyncContext} from {@code before()} to
 * {@code after()} inside the {@link TraceBlock} they open, instead of looking it up on the target a
 * second time. The context is acquired once, in {@code before()}, through
 * {@link #getAsyncContext(Object, Object[])}; there is no after-side lookup hook, so {@code after()}
 * always finishes the async trace with the context {@code before()} used.
 * <p>
 * The bases return the block through the existing {@link BlockAroundInterceptor} shapes, so the
 * weaver, the scope wrappers and the guard code generation are unchanged.
 */
public abstract class AbstractAsyncContextTraceBlockInterceptor {
    protected final PluginLogger logger = PluginLogManager.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();
    protected final boolean asyncTraceBlock;

    public AbstractAsyncContextTraceBlockInterceptor(TraceContext traceContext, boolean asyncTraceBlock) {
        Objects.requireNonNull(traceContext, "traceContext");
        this.asyncTraceBlock = asyncTraceBlock;
    }

    /**
     * The only lookup hook: the AsyncContext to continue, or {@code null} to trace nothing.
     * Called from {@code before()} only.
     */
    protected AsyncContext getAsyncContext(Object target, Object[] args) {
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    protected Trace getAsyncTrace(AsyncContext asyncContext) {
        final Trace trace = asyncContext.continueAsyncTraceObject(asyncTraceBlock);
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

    /**
     * The AsyncContext {@code before()} opened {@code block} with, or {@code null} when the block
     * is not an {@link AsyncContextTraceBlock} (a Trace implementation that does not support
     * {@code getTraceBlock(AsyncContext)}).
     */
    protected AsyncContext asyncContextOf(TraceBlock block) {
        if (block instanceof AsyncContextTraceBlock) {
            return ((AsyncContextTraceBlock) block).getAsyncContext();
        }
        return null;
    }

    /**
     * Closes the async trace and releases the context's thread binding. {@code asyncContext} is
     * {@code null} only on the degraded path where the block carried no context: the trace is still
     * closed, but its thread binding cannot be released here.
     */
    protected void closeAsyncTrace(final Trace trace, final AsyncContext asyncContext) {
        if (isDebug) {
            logger.debug("Close async trace {}.", trace);
        }
        if (asyncContext == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("The trace block carries no AsyncContext; closing the async trace without releasing the AsyncContext. interceptor={}, trace={}", getClass().getName(), trace);
            }
            trace.close();
            return;
        }
        trace.close();
        asyncContext.close();
    }
}
