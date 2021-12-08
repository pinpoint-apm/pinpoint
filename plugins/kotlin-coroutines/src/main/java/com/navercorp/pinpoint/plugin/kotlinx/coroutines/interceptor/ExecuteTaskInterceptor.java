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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.CoroutinesConstants;

/**
 * @author Taejin Koo
 */
public class ExecuteTaskInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public ExecuteTaskInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args) {
        if (ArrayUtils.getLength(args) != 1) {
            return null;
        }

        Object asyncContextAccessor  = args[0];
        if (asyncContextAccessor instanceof AsyncContextAccessor) {
            AsyncContext asyncContext = ((AsyncContextAccessor) asyncContextAccessor)._$PINPOINT$_getAsyncContext();
            return asyncContext;
        }

        return null;
    }

    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        return getAsyncContext(target, args);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(CoroutinesConstants.SERVICE_TYPE);
        recorder.recordException(throwable);
    }

}
