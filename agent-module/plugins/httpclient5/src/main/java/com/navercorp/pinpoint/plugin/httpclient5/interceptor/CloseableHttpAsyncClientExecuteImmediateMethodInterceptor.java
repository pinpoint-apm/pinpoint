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
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5Constants;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5PluginConfig;
import org.apache.hc.client5.http.async.AsyncExecChain;

public class CloseableHttpAsyncClientExecuteImmediateMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private final boolean markError;

    public CloseableHttpAsyncClientExecuteImmediateMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        this.markError = HttpClient5PluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    protected Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        // AsyncExecChain.Scope
        AsyncContext asyncContext = null;
        final AsyncExecChain.Scope scope = ArrayArgumentUtils.getArgument(args, 2, AsyncExecChain.Scope.class);
        if (scope != null) {
            asyncContext = AsyncContextAccessorUtils.getAsyncContext(scope.clientContext);
            if (asyncContext == null) {
                asyncContext = recorder.recordNextAsyncContext();
                AsyncContextAccessorUtils.setAsyncContext(asyncContext, scope.clientContext);
            }
        }

        AsyncContextAccessor asyncContextAccessor = ArrayArgumentUtils.getArgument(args, 3, AsyncContextAccessor.class);
        if (asyncContextAccessor == null) {
            asyncContextAccessor = ArrayArgumentUtils.getArgument(args, 4, AsyncContextAccessor.class);
        }
        if (asyncContextAccessor != null) {
            if (asyncContext == null) {
                asyncContext = recorder.recordNextAsyncContext();
            }
            asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    @Override
    public void afterTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (trace.canSampled()) {
            recorder.recordApi(methodDescriptor);
            recorder.recordException(markError, throwable);
            recorder.recordServiceType(HttpClient5Constants.HTTP_CLIENT5_INTERNAL);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
    }
}
