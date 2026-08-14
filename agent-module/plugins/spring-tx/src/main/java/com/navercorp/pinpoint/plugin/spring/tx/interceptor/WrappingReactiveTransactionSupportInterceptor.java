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

package com.navercorp.pinpoint.plugin.spring.tx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;
import com.navercorp.pinpoint.plugin.spring.tx.SpringTxConfig;
import com.navercorp.pinpoint.plugin.spring.tx.SpringTxConstants;

/**
 * Wrapping variant of {@link ReactiveTransactionSupportInterceptor} (config-gated by
 * {@code profiler.spring.tx.wrap.publisher}). A reactor Mono/Flux result is replaced with a
 * wrapped one instead of relying on the accessor field the reactor plugin injects into
 * reactor.core.publisher types; any other async result keeps the original injection.
 */
public class WrappingReactiveTransactionSupportInterceptor extends SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin {
    private final boolean markError;

    public WrappingReactiveTransactionSupportInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        final SpringTxConfig config = new SpringTxConfig(traceContext.getProfilerConfig());
        this.markError = config.isMarkError();
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(SpringTxConstants.SPRING_TX);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordException(markError, throwable);
        recorder.recordApi(methodDescriptor);
    }

    @Override
    protected Object replaceResult(SpanEventRecorder recorder, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return result;
        }

        if (SeamPublisherWrapper.isWrappable(result)) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            final Object wrapped = SeamPublisherWrapper.wrap(result, asyncContext);
            if (isDebug) {
                logger.debug("Wrapped result publisher. asyncContext={}", asyncContext);
            }
            return wrapped;
        }
        // non-reactor async result: keep the original injection.
        if (result instanceof AsyncContextAccessor) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
        }
        return result;
    }
}
