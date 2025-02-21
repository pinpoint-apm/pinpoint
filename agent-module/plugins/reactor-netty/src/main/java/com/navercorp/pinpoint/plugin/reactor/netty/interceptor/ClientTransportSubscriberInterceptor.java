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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyPluginConfig;

public class ClientTransportSubscriberInterceptor extends AsyncContextSpanEventApiIdAwareAroundInterceptor {
    private final boolean traceTransportError;
    private final boolean markErrorTransportError;

    public ClientTransportSubscriberInterceptor(TraceContext traceContext) {
        super(traceContext);
        final ReactorNettyPluginConfig config = new ReactorNettyPluginConfig(traceContext.getProfilerConfig());
        this.traceTransportError = config.isTraceTransportError();
        this.markErrorTransportError = config.isMarkErrorTransportError();
    }

    // AsyncContext must exist in Target for tracking.
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        if (traceTransportError) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
    }

    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (traceTransportError) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        return null;
    }

    @Override
    public void afterTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (traceTransportError && trace.canSampled()) {
            recorder.recordServiceType(ReactorNettyConstants.REACTOR_NETTY_CLIENT_INTERNAL);
            recorder.recordApiId(apiId);
            final Throwable argThrowable = ArrayArgumentUtils.getArgument(args, 0, Throwable.class);
            if (argThrowable != null) {
                recorder.recordException(markErrorTransportError, argThrowable);
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }
}
