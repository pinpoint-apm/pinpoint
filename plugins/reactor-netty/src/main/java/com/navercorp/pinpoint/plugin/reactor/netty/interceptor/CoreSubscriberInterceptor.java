/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyPluginConfig;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class CoreSubscriberInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private boolean isTraceSubscribeError;
    private List<String> traceSubscribeErrorExcludeMessageList;

    public CoreSubscriberInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
        final ReactorNettyPluginConfig config = new ReactorNettyPluginConfig(traceContext.getProfilerConfig());
        this.isTraceSubscribeError = config.isTraceSubscribeError();
        this.traceSubscribeErrorExcludeMessageList = config.getTraceSubscribeErrorExcludeMessageList();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        if (this.isTraceSubscribeError) {
            final Object th = ArrayUtils.get(args, 0);
            if (th instanceof Throwable) {
                final Throwable exception = (Throwable) th;
                if (!hasExcludeMessage(exception)) {
                    recorder.recordException(exception);
                }
            }
        }
    }

    private boolean hasExcludeMessage(final Throwable throwable) {
        final String exceptionMessage = throwable.getMessage();
        if (exceptionMessage != null && CollectionUtils.hasLength(traceSubscribeErrorExcludeMessageList)) {
            for (String excludeMessage : traceSubscribeErrorExcludeMessageList) {
                if (exceptionMessage.startsWith(excludeMessage)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(ReactorNettyConstants.REACTOR_NETTY_INTERNAL);
        recorder.recordException(throwable);
    }
}