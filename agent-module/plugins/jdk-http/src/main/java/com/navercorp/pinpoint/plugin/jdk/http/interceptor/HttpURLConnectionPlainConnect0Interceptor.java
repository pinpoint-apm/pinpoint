/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdk.http.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpClientRequestAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpConstants;

import java.net.HttpURLConnection;

public class HttpURLConnectionPlainConnect0Interceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final ClientRequestAdaptor<HttpURLConnection> clientRequestAdaptor = new JdkHttpClientRequestAdaptor();

    public HttpURLConnectionPlainConnect0Interceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(JdkHttpConstants.SERVICE_TYPE_INTERNAL);
        if (target instanceof HttpURLConnection) {
            final HttpURLConnection request = (HttpURLConnection) target;
            String destinationId = clientRequestAdaptor.getDestinationId(request);
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, destinationId);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordException(throwable);
    }
}
