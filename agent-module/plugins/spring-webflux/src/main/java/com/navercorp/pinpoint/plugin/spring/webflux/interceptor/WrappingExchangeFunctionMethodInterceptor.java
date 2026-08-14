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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;

/**
 * Wrapping variant of {@link ExchangeFunctionMethodInterceptor} (config-gated by
 * {@code profiler.spring.webflux.wrap.publisher}). The request (args[0]) still receives the
 * AsyncContext through its injected accessor field, but the returned response publisher is
 * replaced with a wrapped one instead of being injected.
 */
public class WrappingExchangeFunctionMethodInterceptor extends SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin {

    public WrappingExchangeFunctionMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX);
        if (args != null && args.length > 0 && args[0] instanceof AsyncContextAccessor) {
            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set closeable-AsyncContext {}", asyncContext);
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    @Override
    protected Object replaceResult(SpanEventRecorder recorder, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        if (throwable != null || !SeamPublisherWrapper.isWrappable(result)) {
            return result;
        }

        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        if (asyncContext == null) {
            return result;
        }
        final Object wrapped = SeamPublisherWrapper.wrap(result, asyncContext);
        if (isDebug) {
            logger.debug("Wrapped response publisher. asyncContext={}", asyncContext);
        }
        return wrapped;
    }
}
