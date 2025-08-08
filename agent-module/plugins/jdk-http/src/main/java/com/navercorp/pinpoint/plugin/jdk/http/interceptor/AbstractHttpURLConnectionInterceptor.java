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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.plugin.jdk.http.HttpURLConnectionClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpClientRequestAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpConstants;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpPluginConfig;

import java.net.HttpURLConnection;

/**
 * @author netspider
 * @author emeroad
 * @author yjqg6666
 */
public abstract class AbstractHttpURLConnectionInterceptor extends SpanEventBlockSimpleAroundInterceptorForPlugin {
    private final ClientRequestRecorder<HttpURLConnection> clientRequestRecorder;
    private final RequestTraceWriter<HttpURLConnection> requestTraceWriter;
    private final ClientRequestAdaptor<HttpURLConnection> clientRequestAdaptor = new JdkHttpClientRequestAdaptor();
    private final boolean markError;

    public AbstractHttpURLConnectionInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);

        boolean param = JdkHttpPluginConfig.isParam(traceContext.getProfilerConfig());
        this.clientRequestRecorder = new ClientRequestRecorder<>(param, clientRequestAdaptor);
        final ClientHeaderAdaptor<HttpURLConnection> clientHeaderAdaptor = new HttpURLConnectionClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
        this.markError = JdkHttpPluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public boolean checkBeforeTraceBlockBegin(Trace trace, Object target, Object[] args) {
        final HttpURLConnection request = (HttpURLConnection) target;
        if (request == null) {
            return false;
        }

        if (requestTraceWriter.isNested(request)) {
            return false;
        }

        if (Boolean.FALSE == trace.canSampled()) {
            this.requestTraceWriter.write(request);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args) {
        final HttpURLConnection request = (HttpURLConnection) target;
        if (request == null) {
            return;
        }

        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        String host = this.clientRequestAdaptor.getDestinationId(request);
        this.requestTraceWriter.write(request, nextId, host);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        recorder.recordServiceType(JdkHttpConstants.SERVICE_TYPE);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(markError, throwable);

        final HttpURLConnection request = (HttpURLConnection) target;
        this.clientRequestRecorder.record(recorder, request, throwable);
    }
}
