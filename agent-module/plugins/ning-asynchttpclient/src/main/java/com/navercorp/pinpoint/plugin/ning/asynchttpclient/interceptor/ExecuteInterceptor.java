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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
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
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientRequestAdaptorV2;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingCookieExtractorV2;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingEntityExtractorV2;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.RequestHeaderAdaptorV2;
import org.asynchttpclient.Request;

/**
 * @author jaehong.kim
 */
public class ExecuteInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    private final ClientRequestRecorder<Request> clientRequestRecorder;
    private final RequestTraceWriter<Request> requestTraceWriter;
    private final CookieRecorder<Request> cookieRecorder;
    private final EntityRecorder<Request> entityRecorder;
    private final boolean markError;

    public ExecuteInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;

        boolean param = NingAsyncHttpClientPluginConfig.isParam(traceContext.getProfilerConfig());
        HttpDumpConfig httpDumpConfig = NingAsyncHttpClientPluginConfig.getHttpDumpConfig(traceContext.getProfilerConfig());
        ClientRequestAdaptor<Request> clientRequestAdaptor = new NingAsyncHttpClientRequestAdaptorV2();
        this.clientRequestRecorder = new ClientRequestRecorder<>(param, clientRequestAdaptor);

        CookieExtractor<Request> cookieExtractor = NingCookieExtractorV2.INSTANCE;
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(httpDumpConfig, cookieExtractor);

        EntityExtractor<Request> entityExtractor = NingEntityExtractorV2.INSTANCE;
        this.entityRecorder = EntityRecorderFactory.newEntityRecorder(httpDumpConfig, entityExtractor);

        ClientHeaderAdaptor<Request> clientHeaderAdaptor = new RequestHeaderAdaptorV2();
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
            final Request httpRequest = getHttpRequest(args);
            if (httpRequest == null) {
                return;
            }

            final SpanEventRecorder recorder = trace.traceBlockBegin();
            if (trace.canSampled()) {
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                AsyncContextAccessorUtils.setAsyncContext(asyncContext, args, 1);

                final TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());
                recorder.recordServiceType(NingAsyncHttpClientConstants.ASYNC_HTTP_CLIENT);
                String host = getHost(httpRequest);
                this.requestTraceWriter.write(httpRequest, nextId, host);
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
            final Request httpRequest = getHttpRequest(args);
            if (httpRequest == null) {
                return;
            }

            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (trace.canSampled()) {
                // Accessing httpRequest here not BEFORE() because it can cause side effect.
                this.clientRequestRecorder.record(recorder, httpRequest, throwable);
                this.cookieRecorder.record(recorder, httpRequest, throwable);
                this.entityRecorder.record(recorder, httpRequest, throwable);

                recorder.recordApi(descriptor);
                recorder.recordException(markError, throwable);

                final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 1);
                if (asyncContext != null) {
                    AsyncContextAccessorUtils.setAsyncContext(asyncContext, result);
                }
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private Request getHttpRequest(final Object[] args) {
        Request request = ArrayArgumentUtils.getArgument(args, 0, Request.class);
        if (request == null) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. expected=Request, args={}.", args);
            }
        }
        return request;
    }
}