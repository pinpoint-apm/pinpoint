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
package com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor;

import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
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
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityRecorderFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.EndPointUtils;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientConstants;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientPluginConfig;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientRequestAdaptorV1;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingCookieExtractorV1;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingEntityExtractorV1;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.RequestHeaderAdaptorV1;
import com.ning.http.client.Request;

/**
 * intercept com.ning.http.client.AsyncHttpClient.executeRequest(Request,
 * AsyncHandler<T>)
 *
 * @author netspider
 * @author jaehong.kim
 */
public class ExecuteRequestInterceptor extends SpanEventBlockSimpleAroundInterceptorForPlugin {
    private final ClientRequestRecorder<Request> clientRequestRecorder;
    private final CookieRecorder<Request> cookieRecorder;
    private final EntityRecorder<Request> entityRecorder;

    private final RequestTraceWriter<Request> requestTraceWriter;
    private final boolean markError;

    // for 1.8.x and 1.9.x
    public ExecuteRequestInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);

        boolean param = NingAsyncHttpClientPluginConfig.isParam(traceContext.getProfilerConfig());
        HttpDumpConfig httpDumpConfig = NingAsyncHttpClientPluginConfig.getHttpDumpConfig(traceContext.getProfilerConfig());
        ClientRequestAdaptor<Request> clientRequestAdaptor = new NingAsyncHttpClientRequestAdaptorV1();
        this.clientRequestRecorder = new ClientRequestRecorder<>(param, clientRequestAdaptor);

        CookieExtractor<Request> cookieExtractor = NingCookieExtractorV1.INSTANCE;
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(httpDumpConfig, cookieExtractor);

        EntityExtractor<Request> entityExtractor = NingEntityExtractorV1.INSTANCE;
        this.entityRecorder = EntityRecorderFactory.newEntityRecorder(httpDumpConfig, entityExtractor);

        ClientHeaderAdaptor<Request> clientHeaderAdaptor = new RequestHeaderAdaptorV1();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
        this.markError = NingAsyncHttpClientPluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public boolean checkBeforeTraceBlockBegin(Trace trace, Object target, Object[] args) {
        Request httpRequest = getHttpReqeust(args);
        if (httpRequest == null) {
            return false;
        }

        if (requestTraceWriter.isNested(httpRequest)) {
            return false;
        }

        if (Boolean.FALSE == trace.canSampled()) {
            this.requestTraceWriter.write(httpRequest);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args) {
        Request httpRequest = getHttpReqeust(args);
        if (httpRequest == null) {
            return;
        }

        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        String host = getHost(httpRequest);
        requestTraceWriter.write(httpRequest, nextId, host);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        recorder.recordServiceType(NingAsyncHttpClientConstants.ASYNC_HTTP_CLIENT);
    }

    private String getHost(Request httpRequest) {
        return EndPointUtils.getEndPoint(httpRequest.getUrl(), null);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(markError, throwable);

        Request httpRequest = getHttpReqeust(args);
        if (httpRequest == null) {
            return;
        }

        // Accessing httpRequest here not BEFORE() because it can cause side effect.
        this.clientRequestRecorder.record(recorder, httpRequest, throwable);
        this.cookieRecorder.record(recorder, httpRequest, throwable);
        this.entityRecorder.record(recorder, httpRequest, throwable);
    }

    private Request getHttpReqeust(final Object[] args) {
        final Request request = ArrayArgumentUtils.getArgument(args, 0, Request.class);
        return request;
    }
}