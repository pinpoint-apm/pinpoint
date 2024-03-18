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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityRecorderFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.httpclient5.HostUtils;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5Constants;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5CookieExtractor;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5EntityExtractor;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5PluginConfig;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5RequestWrapper;
import com.navercorp.pinpoint.plugin.httpclient5.HttpRequest5ClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.httpclient5.HttpRequestGetter;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;

public class CloseableHttpAsyncClientDoExecuteInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final CookieRecorder<HttpRequest> cookieRecorder;
    private final EntityRecorder<HttpRequest> entityRecorder;

    private final RequestTraceWriter<HttpRequest> requestTraceWriter;

    public CloseableHttpAsyncClientDoExecuteInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;

        final HttpClient5PluginConfig config = new HttpClient5PluginConfig(traceContext.getProfilerConfig());
        final boolean param = config.isParam();
        final HttpDumpConfig httpDumpConfig = config.getHttpDumpConfig();

        ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<>(param, clientRequestAdaptor);

        CookieExtractor<HttpRequest> cookieExtractor = HttpClient5CookieExtractor.INSTANCE;
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(httpDumpConfig, cookieExtractor);

        EntityExtractor<HttpRequest> entityExtractor = HttpClient5EntityExtractor.INSTANCE;
        this.entityRecorder = EntityRecorderFactory.newEntityRecorder(httpDumpConfig, entityExtractor);

        ClientHeaderAdaptor<HttpRequest> clientHeaderAdaptor = new HttpRequest5ClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
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
            final HttpHost httpHost = ArrayArgumentUtils.getArgument(args, 0, HttpHost.class);
            final HttpRequest httpRequest = getHttpRequest(args);
            if (httpRequest == null) {
                return;
            }

            final String host = HostUtils.get(httpHost, httpRequest);
            final boolean sampling = trace.canSampled();
            if (!sampling) {
                if (httpRequest != null) {
                    this.requestTraceWriter.write(httpRequest);
                }
                return;
            }

            final SpanEventRecorder recorder = trace.traceBlockBegin();
            // set remote trace
            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            recorder.recordServiceType(HttpClient5Constants.HTTP_CLIENT5);
            this.requestTraceWriter.write(httpRequest, nextId, host);
            // HttpContext
            final AsyncContextAccessor asyncContextAccessor = ArrayArgumentUtils.getArgument(args, 4, AsyncContextAccessor.class);
            if (asyncContextAccessor != null) {
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final HttpHost httpHost = ArrayArgumentUtils.getArgument(args, 0, HttpHost.class);
            final HttpRequest httpRequest = getHttpRequest(args);
            if (httpRequest == null) {
                return;
            }
            final String host = HostUtils.get(httpHost, httpRequest);
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            // Accessing httpRequest here not BEFORE() because it can cause side effect.
            ClientRequestWrapper clientRequest = new HttpClient5RequestWrapper(httpRequest, host);
            this.clientRequestRecorder.record(recorder, clientRequest, throwable);
            this.cookieRecorder.record(recorder, httpRequest, throwable);
            this.entityRecorder.record(recorder, httpRequest, throwable);
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
            if (result instanceof AsyncContextAccessor) {
                // HttpContext
                final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 4);
                if (asyncContext != null) {
                    ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
                }
            }
        } catch (Throwable t) {
            logger.warn("Failed to AFTER process. {}", t.getMessage(), t);
        } finally {
            trace.traceBlockEnd();
        }
    }

    HttpRequest getHttpRequest(final Object[] args) {
        final HttpRequestGetter httpRequestGetter = ArrayArgumentUtils.getArgument(args, 1, HttpRequestGetter.class);
        if (httpRequestGetter == null) {
            return null;
        }
        final HttpRequest httpRequest = httpRequestGetter._$PINPOINT$_getHttpRequest();
        return httpRequest;
    }
}
