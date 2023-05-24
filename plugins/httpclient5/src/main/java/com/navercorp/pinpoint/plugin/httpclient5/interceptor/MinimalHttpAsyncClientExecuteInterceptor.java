/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.httpclient5.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5Constants;

public class MinimalHttpAsyncClientExecuteInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    public MinimalHttpAsyncClientExecuteInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        final AsyncContextAccessor asyncContextAccessor = ArrayArgumentUtils.getArgument(args, 0, AsyncContextAccessor.class);
        if (asyncContextAccessor != null) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    @Override
    public void afterTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (trace.canSampled()) {
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
            recorder.recordServiceType(HttpClient5Constants.HTTP_CLIENT5_INTERNAL);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
    }
}
