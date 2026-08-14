/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.InjectedAsyncContextSpanEventApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.plugin.reactor.ReactorPluginConfig;

/**
 * Restores the exact retry subscriber's seed only while Reactor performs the source
 * resubscription. It records no event per attempt and reads the injected field directly through
 * the weaver, keeping the hot call site monomorphic.
 * <p>
 * Closing a window closes and unbinds that attempt's temporary async trace, not the seed. The
 * subscriber can therefore reuse the same {@link AsyncContext} for later retry attempts.
 */
public class RetrySubscriberResubscribeInterceptor extends InjectedAsyncContextSpanEventApiIdAwareAroundInterceptor {
    private final boolean traceRetry;

    public RetrySubscriberResubscribeInterceptor(TraceContext traceContext) {
        super(traceContext, false);
        this.traceRetry = ReactorPluginConfig.isTraceRetry(traceContext.getProfilerConfig());
    }

    @Override
    public void before(Object target, AsyncContext asyncContext, int apiId, Object[] args) {
        if (traceRetry) {
            super.before(target, asyncContext, apiId, args);
        }
    }

    @Override
    public void after(Object target, AsyncContext asyncContext, int apiId, Object[] args, Object result, Throwable throwable) {
        if (traceRetry) {
            super.after(target, asyncContext, apiId, args, result, throwable);
        }
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
        // restore-only: never reached with asyncTraceBlock=false.
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        // restore-only: never reached with asyncTraceBlock=false.
    }

    @Override
    protected void deleteAsyncContext(Trace trace, AsyncContext asyncContext) {
        // Cleanup must be best-effort and independent. In particular, a Trace.close() failure must
        // never prevent the AsyncContext from unbinding the delivery thread or escape into Reactor's
        // resubscribe control flow.
        try {
            trace.close();
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close retry resubscribe trace. Caused:{}", th.getMessage(), th);
            }
        }
        try {
            asyncContext.close();
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to unbind retry resubscribe trace. Caused:{}", th.getMessage(), th);
            }
        }
    }
}
