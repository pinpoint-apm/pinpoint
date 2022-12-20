/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author jaehong.kim
 */
public class DispatchHandlerInvokeHandlerMethodInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private TraceContext traceContext;
    private final Boolean uriStatEnable;
    private final Boolean uriStatUseUserInput;

    public DispatchHandlerInvokeHandlerMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, Boolean uriStatEnable, Boolean uriStatUseUserInput) {
        super(traceContext, methodDescriptor);
        this.traceContext = traceContext;
        this.uriStatEnable = uriStatEnable;
        this.uriStatUseUserInput = uriStatUseUserInput;
    }

    // BEFORE
    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        if (validate(args)) {
            return AsyncContextAccessorUtils.getAsyncContext(args, 0);
        }
        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        if (uriStatEnable && uriStatUseUserInput) {
            final Trace trace = traceContext.currentRawTraceObject();
            if (trace == null) {
                return;
            }

            final ServerWebExchange exchange = ArrayArgumentUtils.getArgument(args, 0, ServerWebExchange.class);
            if (exchange != null) {
                for (String attributeName : SpringWebFluxConstants.SPRING_WEBFLUX_URI_USER_INPUT_ATTRIBUTE_KEYS) {
                    final Object uriMapping = exchange.getAttribute(attributeName);
                    if (!(uriMapping instanceof String)) {
                        continue;
                    }

                    final String uriTemplate = (String) uriMapping;
                    if (StringUtils.hasLength(uriTemplate)) {
                        final SpanRecorder spanRecorder = trace.getSpanRecorder();
                        spanRecorder.recordUriTemplate(uriTemplate, true);
                    }
                }
            }
        }
    }

    // AFTER
    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (validate(args)) {
            return AsyncContextAccessorUtils.getAsyncContext(args, 0);
        }
        return null;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX);
        recorder.recordException(throwable);

        if (Boolean.FALSE == validate(args)) {
            return;
        }

        final AsyncContext publisherAsyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        if (publisherAsyncContext != null) {
            // Set AsyncContext to CoreSubscriber
            if (result instanceof AsyncContextAccessor) {
                ((AsyncContextAccessor) (result))._$PINPOINT$_setAsyncContext(publisherAsyncContext);
                if (isDebug) {
                    logger.debug("Set AsyncContext to result. asyncContext={}", publisherAsyncContext);
                }
            }
        }
    }

    private boolean validate(final Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return false;
        }
        return true;
    }
}