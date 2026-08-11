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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;

/**
 * Experimental {@code publishOn} seam. The transform selects this interceptor instead of
 * {@link FluxAndMonoPublishOnInterceptor}, so one returned publisher is owned by either the
 * wrapper or the legacy injected field, never both.
 */
public class WrappingFluxAndMonoPublishOnInterceptor extends SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin {

    public WrappingFluxAndMonoPublishOnInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(ReactorConstants.REACTOR);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    @Override
    protected Object replaceResult(SpanEventRecorder recorder, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        // Check before minting. Unsupported publisher shapes keep the exact legacy no-op and
        // must not leave a dangling async link.
        if (throwable != null || !SeamPublisherWrapper.isWrappable(result)) {
            return result;
        }

        final AsyncContext asyncContext = recorder.recordNextAsyncContext();
        final Object wrapped = SeamPublisherWrapper.wrap(result, asyncContext);
        if (wrapped != result && returnType.isInstance(wrapped)) {
            if (isDebug) {
                logger.debug("Wrapped publishOn result. asyncContext={}", asyncContext);
            }
            return wrapped;
        }

        // A wrapping failure must not strand the link that was just minted. Runtime Reactor
        // publishers normally have the legacy accessor, so use it only as a failure fallback.
        if (result instanceof AsyncContextAccessor) {
            ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Fell back to publishOn field injection. asyncContext={}", asyncContext);
            }
        }
        return result;
    }
}
