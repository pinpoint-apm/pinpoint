/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;
import com.navercorp.pinpoint.plugin.reactor.ReactorPluginConfig;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class CoreSubscriberInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    private boolean isTraceSubscribeError;
    private List<String> traceSubscribeErrorExcludeMessageList;

    public CoreSubscriberInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);

        final ReactorPluginConfig config = new ReactorPluginConfig(traceContext.getProfilerConfig());
        isTraceSubscribeError = config.isTraceSubscribeError();
        this.traceSubscribeErrorExcludeMessageList = config.getTraceSubscribeErrorExcludeMessageList();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        if (this.isTraceSubscribeError) {
            if (args != null && args.length >= 1 && (args[0] instanceof Throwable)) {
                final Throwable exception = (Throwable) args[0];
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
        recorder.recordServiceType(ReactorConstants.REACTOR_NETTY);
        recorder.recordException(throwable);
    }
}