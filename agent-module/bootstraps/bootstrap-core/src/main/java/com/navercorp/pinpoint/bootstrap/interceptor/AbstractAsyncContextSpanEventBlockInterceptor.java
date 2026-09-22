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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;

/**
 * Shared part of the async Block bases. after() always closes the {@link com.navercorp.pinpoint.bootstrap.context.TraceBlock}
 * that before() opened and leaves the async trace scope, even when it cannot look the
 * {@link AsyncContext} up any more; only the context-bound hooks and the AsyncContext release
 * depend on the context.
 */
public abstract class AbstractAsyncContextSpanEventBlockInterceptor extends AbstractAsyncContextSpanEventInterceptor {

    public AbstractAsyncContextSpanEventBlockInterceptor(TraceContext traceContext, boolean asyncTraceBlock) {
        super(traceContext, asyncTraceBlock);
    }

    /**
     * Closes the async trace and releases the context's thread binding. Without a context the
     * trace is still closed, but its thread binding cannot be released here.
     */
    @Override
    protected void deleteAsyncContext(final Trace trace, final AsyncContext asyncContext) {
        if (asyncContext == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("AsyncContext is missing in after(); closing the async trace without releasing the AsyncContext. interceptor={}, trace={}", getClass().getName(), trace);
            }
            trace.close();
            return;
        }
        super.deleteAsyncContext(trace, asyncContext);
    }
}
