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
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
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
public class ExecuteRequestInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(ExecuteRequestInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    private final ClientRequestRecorder<Request> clientRequestRecorder;
    private final CookieRecorder<Request> cookieRecorder;
    private final EntityRecorder<Request> entityRecorder;

    private final RequestTraceWriter<Request> requestTraceWriter;
    private final boolean markError;

    // for 1.8.x and 1.9.x
    public ExecuteRequestInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;

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
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            Request httpRequest = getHttpReqeust(args);
            if (httpRequest == null) {
                return;
            }

            final SpanEventRecorder recorder = trace.traceBlockBegin();
            if (trace.canSampled()) {
                final TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());
                recorder.recordServiceType(NingAsyncHttpClientConstants.ASYNC_HTTP_CLIENT);

                String host = getHost(httpRequest);
                requestTraceWriter.write(httpRequest, nextId, host);
            } else {
                this.requestTraceWriter.write(httpRequest);
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private String getHost(Request httpRequest) {
        return EndPointUtils.getEndPoint(httpRequest.getUrl(), null);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // Do not log result
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            Request httpReqeust = getHttpReqeust(args);
            if (httpReqeust == null) {
                return;
            }

            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (trace.canSampled()) {
                final Request httpRequest = (Request) args[0];
                // Accessing httpRequest here not BEFORE() because it can cause side effect.
                this.clientRequestRecorder.record(recorder, httpRequest, throwable);
                this.cookieRecorder.record(recorder, httpRequest, throwable);
                this.entityRecorder.record(recorder, httpRequest, throwable);

                recorder.recordApi(descriptor);
                recorder.recordException(markError, throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private Request getHttpReqeust(final Object[] args) {
        final Request request = ArrayArgumentUtils.getArgument(args, 0, Request.class);
        if (request == null) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. args={}.", args);
            }
        }

        return request;
    }
}