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
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;

/**
 * @author jaehong.kim
 */
public class DispatchHandlerInvokeHandlerMethodInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public DispatchHandlerInvokeHandlerMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args) {
        if (args != null && args.length >= 1) {
            return AsyncContextAccessorUtils.getAsyncContext(args[0]);
        }
        return null;
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX);
        recorder.recordException(throwable);

        final AsyncContext publisherAsyncContext = getAsyncContext(target, args);
        if (publisherAsyncContext != null) {
            // Set AsyncContext to CoreSubscriber
            if (result instanceof AsyncContextAccessor) {
                ((AsyncContextAccessor) (result))._$PINPOINT$_setAsyncContext(publisherAsyncContext);
                if (isDebug) {
                    logger.debug("Set AsyncContext result={}", result);
                }
            }
        }
    }
}