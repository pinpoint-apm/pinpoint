/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;
import com.navercorp.pinpoint.plugin.hystrix.descriptor.HystrixCommandTimeoutTimerMethodDescriptor;
import com.navercorp.pinpoint.plugin.hystrix.field.EnclosingInstanceAccessor;

/**
 * @author HyunGil Jeong
 */
public class HystrixObservableTimeoutListenerTickInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    private static final HystrixCommandTimeoutTimerMethodDescriptor HYSTRIX_COMMAND_TIMEOUT_TIMER_METHOD_DESCRIPTOR = new HystrixCommandTimeoutTimerMethodDescriptor();

    public HystrixObservableTimeoutListenerTickInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        traceContext.cacheApi(HYSTRIX_COMMAND_TIMEOUT_TIMER_METHOD_DESCRIPTOR);
    }

    @Override
    protected AsyncContext getAsyncContext(Object target) {
        return getAsyncContext(target, null);
    }

    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args) {
        if (target instanceof EnclosingInstanceAccessor) {
            return AsyncContextAccessorUtils.getAsyncContext(((EnclosingInstanceAccessor) target)._$PINPOINT$_getEnclosingInstance());
        }
        return null;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE);
        recorder.recordApi(HYSTRIX_COMMAND_TIMEOUT_TIMER_METHOD_DESCRIPTOR);
        recorder.recordException(throwable);
    }
}
