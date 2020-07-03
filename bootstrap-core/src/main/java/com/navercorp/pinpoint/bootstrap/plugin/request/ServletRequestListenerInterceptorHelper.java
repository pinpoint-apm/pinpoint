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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.method.ServletSyncMethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class ServletRequestListenerInterceptorHelper<REQ> {
    private static final MethodDescriptor SERVLET_SYNC_METHOD_DESCRIPTOR = new ServletSyncMethodDescriptor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final TraceContext traceContext;
    private final ServiceType serviceType;
    private final RequestAdaptor<REQ> requestAdaptor;

    private final Filter<String> excludeUrlFilter;
    private final RequestTraceReader<REQ> requestTraceReader;
    private final ServerRequestRecorder<REQ> serverRequestRecorder;
    private final HttpStatusCodeRecorder httpStatusCodeRecorder;

    private final ParameterRecorder<REQ> parameterRecorder;
    private final RequestRecorderFactory<REQ> requestRecorderFactory;
    private final ProxyRequestRecorder<REQ> proxyRequestRecorder;

    @Deprecated
    public ServletRequestListenerInterceptorHelper(final ServiceType serviceType, final TraceContext traceContext, RequestAdaptor<REQ> requestAdaptor, final Filter<String> excludeUrlFilter, ParameterRecorder<REQ> parameterRecorder) {
        this(serviceType, traceContext, requestAdaptor, excludeUrlFilter, parameterRecorder, null);
    }

    public ServletRequestListenerInterceptorHelper(final ServiceType serviceType, final TraceContext traceContext, RequestAdaptor<REQ> requestAdaptor, final Filter<String> excludeUrlFilter, ParameterRecorder<REQ> parameterRecorder, RequestRecorderFactory<REQ> requestRecorderFactory) {
        this.serviceType = Assert.requireNonNull(serviceType, "serviceType");
        this.traceContext = Assert.requireNonNull(traceContext, "traceContext");
        this.requestAdaptor = Assert.requireNonNull(requestAdaptor, "requestAdaptor");
        this.requestTraceReader = new RequestTraceReader<REQ>(traceContext, requestAdaptor, true);
        this.requestRecorderFactory = requestRecorderFactory;
        ProfilerConfig profilerConfig = this.traceContext.getProfilerConfig();
        if (this.requestRecorderFactory != null) {
            proxyRequestRecorder = this.requestRecorderFactory.getProxyRequestRecorder(profilerConfig.isProxyHttpHeaderEnable(), requestAdaptor);
        } else {
            // Compatibility 1.8.1
            proxyRequestRecorder = new ProxyHttpHeaderRecorder<REQ>(profilerConfig.isProxyHttpHeaderEnable(), requestAdaptor);
        }
        this.excludeUrlFilter = defaultFilter(excludeUrlFilter);
        this.parameterRecorder = Assert.requireNonNull(parameterRecorder, "parameterRecorder");

        final ServerHeaderRecorder<REQ> headerRecorder = newServerHeaderRecorder(requestAdaptor, profilerConfig);
        final ServerCookieRecorder<REQ> cookieRecorder = newServerCookieRecorder(requestAdaptor, profilerConfig);
        this.serverRequestRecorder = new ServerRequestRecorder<REQ>(requestAdaptor, headerRecorder, cookieRecorder);
        this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(profilerConfig.getHttpStatusCodeErrors());

        this.traceContext.cacheApi(SERVLET_SYNC_METHOD_DESCRIPTOR);
    }

    private ServerCookieRecorder<REQ> newServerCookieRecorder(RequestAdaptor<REQ> requestAdaptor, ProfilerConfig profilerConfig) {
        List<String> recordRequestCookies = profilerConfig.readList(ServerCookieRecorder.CONFIG_KEY_RECORD_REQ_COOKIES);
        if (recordRequestCookies.isEmpty()) {
            return new BypassServerCookieRecorder<REQ>();
        }

        if (!(requestAdaptor instanceof CookieSupportAdaptor)) {
            // unsupported
            return new BypassServerCookieRecorder<REQ>();
        }
        CookieSupportAdaptor<REQ> cookieAdaptor = (CookieSupportAdaptor<REQ>) requestAdaptor;
        return new DefaultServerCookieRecorder<REQ>(cookieAdaptor, recordRequestCookies);
    }

    private ServerHeaderRecorder<REQ> newServerHeaderRecorder(RequestAdaptor<REQ> requestAdaptor, ProfilerConfig profilerConfig) {
        List<String> recordRequestHeaders = profilerConfig.readList(ServerHeaderRecorder.CONFIG_KEY_RECORD_REQ_HEADERS);
        if (recordRequestHeaders.isEmpty()) {
            return new BypassServerHeaderRecorder<REQ>();
        }
        return new DefaultServerHeaderRecorder<REQ>(requestAdaptor, recordRequestHeaders);
    }

    private <T> Filter<T> defaultFilter(Filter<T> excludeUrlFilter) {
        if (excludeUrlFilter == null) {
            return new SkipFilter<T>();
        }
        return excludeUrlFilter;
    }

    public void initialized(REQ request, final ServiceType serviceType, final MethodDescriptor methodDescriptor) {
        Assert.requireNonNull(request, "request");
        Assert.requireNonNull(serviceType, "serviceType");
        Assert.requireNonNull(methodDescriptor, "methodDescriptor");

        if (isDebug) {
            logger.debug("Initialized requestEvent. request={}, serviceType={}, methodDescriptor={}", request, serviceType, methodDescriptor);
        }

        final Trace trace = createTrace(request);
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(serviceType);
        recorder.recordApi(methodDescriptor);
    }

    private Trace createTrace(REQ request) {
        final String requestURI = requestAdaptor.getRpcName(request);
        if (this.excludeUrlFilter.filter(requestURI)) {
            if (isTrace) {
                logger.trace("Filter requestURI={}", requestURI);
            }
            return null;
        }

        final Trace trace = this.requestTraceReader.read(request);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            // record root span
            recorder.recordServiceType(this.serviceType);
            recorder.recordApi(SERVLET_SYNC_METHOD_DESCRIPTOR);
            this.serverRequestRecorder.record(recorder, request);
            // record proxy HTTP header.
            this.proxyRequestRecorder.record(recorder, request);
        }
        return trace;
    }

    public void destroyed(REQ request, final Throwable throwable, final int statusCode) {
        if (isDebug) {
            logger.debug("Destroyed requestEvent. request={}, throwable={}, statusCode={}", request, throwable, statusCode);
        }

        final Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            trace.close();
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordException(throwable);
            this.httpStatusCodeRecorder.record(trace.getSpanRecorder(), statusCode);
            // Must be executed in destroyed()
            this.parameterRecorder.record(recorder, request, throwable);
        } finally {
            trace.traceBlockEnd();
            this.traceContext.removeTraceObject();
            trace.close();
        }
    }
}