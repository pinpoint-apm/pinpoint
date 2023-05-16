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

package com.navercorp.pinpoint.plugin.user.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayUtils;

/**
 * @author HyunGil Jeong
 */
public class MQExternalClientHandlerInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public MQExternalClientHandlerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args) {
        return getAsyncContext(args);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        return getAsyncContext(args);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(ServiceType.INTERNAL_METHOD);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private AsyncContext getAsyncContext(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof AsyncContextAccessor) {
                return AsyncContextAccessorUtils.getAsyncContext(arg);
            }
        }
        return null;
    }
}
