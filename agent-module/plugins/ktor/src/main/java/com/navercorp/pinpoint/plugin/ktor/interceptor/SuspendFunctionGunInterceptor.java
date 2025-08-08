/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ktor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.ktor.KtorConstants;
import io.ktor.util.pipeline.PipelineContext;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;

public class SuspendFunctionGunInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public SuspendFunctionGunInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        return getAsyncContext(target);
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        return getAsyncContext(target);
    }

    private AsyncContext getAsyncContext(Object object) {
        if (object instanceof PipelineContext) {
            final PipelineContext pipelineContext = (PipelineContext) object;
            final Object context = pipelineContext.getContext();
            if (context instanceof AsyncContextAccessor) {
                return AsyncContextAccessorUtils.getAsyncContext(context);
            }
            if (context instanceof CoroutineScope) {
                final CoroutineScope continuation = (CoroutineScope) context;
                final CoroutineContext coroutineContext = continuation.getCoroutineContext();
                if (coroutineContext instanceof AsyncContextAccessor) {
                    return AsyncContextAccessorUtils.getAsyncContext(coroutineContext);
                }
            }
        }

        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        recorder.recordServiceType(KtorConstants.KTOR_INTERNAL);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }
}