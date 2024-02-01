/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdk.httpclient.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
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
import com.navercorp.pinpoint.plugin.jdk.httpclient.HttpRequestImplClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.jdk.httpclient.HttpRequestImplClientRequestAdaptor;
import com.navercorp.pinpoint.plugin.jdk.httpclient.HttpRequestImplGetter;
import com.navercorp.pinpoint.plugin.jdk.httpclient.JdkHttpClientConstants;
import com.navercorp.pinpoint.plugin.jdk.httpclient.JdkHttpClientCookieExtractor;
import com.navercorp.pinpoint.plugin.jdk.httpclient.JdkHttpClientEntityExtractor;
import com.navercorp.pinpoint.plugin.jdk.httpclient.JdkHttpClientPluginConfig;
import jdk.internal.net.http.HttpRequestImpl;

public class MultiExchangeResponseAsyncImplInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final ClientRequestRecorder<HttpRequestImpl> clientRequestRecorder;

    private final RequestTraceWriter<HttpRequestImpl> requestTraceWriter;
    private final CookieRecorder<HttpRequestImpl> cookieRecorder;
    private final EntityRecorder<HttpRequestImpl> entityRecorder;

    public MultiExchangeResponseAsyncImplInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);

        final JdkHttpClientPluginConfig config = new JdkHttpClientPluginConfig(traceContext.getProfilerConfig());
        final HttpDumpConfig httpDumpConfig = config.getHttpDumpConfig();

        final ClientRequestAdaptor<HttpRequestImpl> clientRequestAdaptor = new HttpRequestImplClientRequestAdaptor();
        this.clientRequestRecorder = new ClientRequestRecorder<HttpRequestImpl>(config.isParam(), clientRequestAdaptor);

        final ClientHeaderAdaptor<HttpRequestImpl> clientHeaderAdaptor = new HttpRequestImplClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<HttpRequestImpl>(clientHeaderAdaptor, traceContext);

        final CookieExtractor<HttpRequestImpl> cookieExtractor = JdkHttpClientCookieExtractor.INSTANCE;
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(httpDumpConfig, cookieExtractor);

        final EntityExtractor<HttpRequestImpl> entityExtractor = JdkHttpClientEntityExtractor.INSTANCE;
        this.entityRecorder = EntityRecorderFactory.newEntityRecorder(httpDumpConfig, entityExtractor);
    }

    @Override
    public void beforeTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, Object[] args) {
        if (Boolean.FALSE == (target instanceof HttpRequestImplGetter)) {
            return;
        }

        final HttpRequestImpl request = ((HttpRequestImplGetter) target)._$PINPOINT$_getCurrentreq();
        if (request == null) {
            return;
        }

        final boolean sampling = trace.canSampled();
        if (!sampling) {
            this.requestTraceWriter.write(request);
            return;
        }

        try {
            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            final String host = HttpRequestImplClientRequestAdaptor.getHost(request);
            this.requestTraceWriter.write(request, nextId, host);
        } catch (Throwable t) {
            logger.warn("Failed to trace write", t);
        }

        if (request instanceof AsyncContextAccessor) {
            final AsyncContext nextAsyncContext = recorder.recordNextAsyncContext();
            AsyncContextAccessorUtils.setAsyncContext(nextAsyncContext, request);
            if (isDebug) {
                logger.debug("Set AsyncContext {}", asyncContext);
            }
        }
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    public void afterTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (Boolean.FALSE == (target instanceof HttpRequestImplGetter)) {
            return;
        }

        final HttpRequestImpl request = ((HttpRequestImplGetter) target)._$PINPOINT$_getCurrentreq();
        if (request == null) {
            return;
        }

        final boolean sampling = trace.canSampled();
        if (!sampling) {
            return;
        }

        try {
            this.clientRequestRecorder.record(recorder, request, throwable);
            this.cookieRecorder.record(recorder, request, throwable);
            this.entityRecorder.record(recorder, request, throwable);
        } catch (Throwable t) {
            logger.warn("Failed to record", t);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(JdkHttpClientConstants.JDK_HTTP_CLIENT);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }
}
