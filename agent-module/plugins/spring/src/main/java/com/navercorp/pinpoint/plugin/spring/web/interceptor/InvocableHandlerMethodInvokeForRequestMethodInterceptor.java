/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * @author jaehong.kim
 */
public class InvocableHandlerMethodInvokeForRequestMethodInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public InvocableHandlerMethodInvokeForRequestMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    public AsyncContext getAsyncContext(Object target, Object[] args) {
        return getAsyncContextFromArgs(args);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        return getAsyncContextFromArgs(args);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        recorder.recordServiceType(SpringWebMvcConstants.SPRING_MVC);
    }

    private AsyncContext getAsyncContextFromArgs(Object[] args) {
        NativeWebRequest request = ArrayArgumentUtils.getArgument(args, 0, NativeWebRequest.class);
        if (request != null) {
            final Object asyncContext = request.getAttribute(AsyncContext.class.getName(), 0);
            if (asyncContext instanceof AsyncContext) {
                return (AsyncContext) asyncContext;
            }
        }

        return null;
    }
}
