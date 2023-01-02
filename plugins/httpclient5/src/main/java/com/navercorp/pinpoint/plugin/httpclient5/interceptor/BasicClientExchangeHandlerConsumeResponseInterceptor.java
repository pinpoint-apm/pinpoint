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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5Constants;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5PluginConfig;
import org.apache.hc.core5.http.HttpResponse;

public class BasicClientExchangeHandlerConsumeResponseInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final boolean statusCode;

    public BasicClientExchangeHandlerConsumeResponseInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        final HttpClient5PluginConfig config = new HttpClient5PluginConfig(traceContext.getProfilerConfig());
        this.statusCode = config.isStatusCode();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        if (statusCode) {
            final HttpResponse httpResponse = ArrayArgumentUtils.getArgument(args, 0, HttpResponse.class);
            if (httpResponse != null) {
                int statusCode = httpResponse.getCode();
                recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, statusCode);
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        recorder.recordServiceType(HttpClient5Constants.HTTP_CLIENT5_INTERNAL);
    }
}
