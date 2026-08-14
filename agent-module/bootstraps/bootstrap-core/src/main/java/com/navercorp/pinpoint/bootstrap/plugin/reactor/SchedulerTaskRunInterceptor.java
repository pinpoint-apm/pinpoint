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
package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.InjectedAsyncContextSpanEventApiIdAwareAroundInterceptor;

/**
 * Activates the trace carried by a scheduler task ({@link SchedulerTaskConstructorInterceptor})
 * around the task body on the worker thread, restore-only: no span event per execution
 * ({@code asyncTraceBlock=false}), the same flow {@code CoreSubscriberOnNextInterceptor} uses for
 * signal delivery.
 * <p>
 * The carried context is supplied by the weaver as a monomorphic read of the injected field —
 * the target classes are final, so no shared megamorphic call site forms.
 * <p>
 * Lifecycle comes from the async trace scope: a task whose {@code run()} delegates to
 * {@code call()} (both woven) enters the scope twice and unbinds/closes only when the outermost
 * frame ends. On an inline executor with a trace already active on the thread, that trace is
 * returned and — not being an async trace — is never closed here, so the ambient trace is
 * preserved.
 */
public class SchedulerTaskRunInterceptor extends InjectedAsyncContextSpanEventApiIdAwareAroundInterceptor {

    public SchedulerTaskRunInterceptor(TraceContext traceContext) {
        super(traceContext, false);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
        // restore-only: never reached with asyncTraceBlock=false.
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        // restore-only: never reached with asyncTraceBlock=false.
    }
}
