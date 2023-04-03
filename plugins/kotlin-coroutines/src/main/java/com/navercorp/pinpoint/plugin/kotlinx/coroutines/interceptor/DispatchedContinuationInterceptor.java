/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;

public class DispatchedContinuationInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public DispatchedContinuationInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        final Continuation continuation = ArrayArgumentUtils.getArgument(args, 1, Continuation.class);
        if (continuation != null) {
            final CoroutineContext context = continuation.getContext();
            return AsyncContextAccessorUtils.getAsyncContext(context);
        }
        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        final Continuation continuation = ArrayArgumentUtils.getArgument(args, 1, Continuation.class);
        if (continuation != null) {
            final CoroutineContext context = continuation.getContext();
            return AsyncContextAccessorUtils.getAsyncContext(context);
        }
        return null;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
    }
}
