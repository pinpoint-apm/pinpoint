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
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.method.ServletRequestListenerMethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.method.ServletSyncMethodDescriptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author jaehong.kim
 */
public class ServletRequestListenerInterceptorHelper {
    private static final MethodDescriptor SERVLET_SYNC_METHOD_DESCRIPTOR = new ServletSyncMethodDescriptor();
    private static final MethodDescriptor SERVLET_REQUEST_LISTENER_METHOD_DESCRIPTOR = new ServletRequestListenerMethodDescriptor();

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final TraceContext traceContext;
    private final boolean isTraceRequestParam;
    private final Filter<String> excludeProfileMethodFilter;
    private final Filter<String> excludeUrlFilter;
    private final RequestTraceReader requestTraceReader;
    private final ProxyHttpHeaderRecorder proxyHttpHeaderRecorder;
    private final ServerRequestRecorder serverRequestRecorder = new ServerRequestRecorder();
    private final HttpStatusCodeRecorder httpStatusCodeRecorder;

    public ServletRequestListenerInterceptorHelper(final TraceContext traceContext, final Filter<String> excludeUrlFilter, final Filter<String> excludeProfileMethodFilter, final boolean isTraceRequestParam) {
        this.traceContext = Assert.requireNonNull(traceContext, "traceContext must not be null");
        this.requestTraceReader = new RequestTraceReader(traceContext, true);
        this.proxyHttpHeaderRecorder = new ProxyHttpHeaderRecorder(traceContext.getProfilerConfig().isProxyHttpHeaderEnable());
        if (excludeUrlFilter != null) {
            this.excludeUrlFilter = excludeUrlFilter;
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        if (excludeProfileMethodFilter != null) {
            this.excludeProfileMethodFilter = excludeProfileMethodFilter;
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
        this.isTraceRequestParam = isTraceRequestParam;
        this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(traceContext.getProfilerConfig().getHttpStatusCodeErrors());

        this.traceContext.cacheApi(SERVLET_SYNC_METHOD_DESCRIPTOR);
        this.traceContext.cacheApi(SERVLET_REQUEST_LISTENER_METHOD_DESCRIPTOR);
    }

    public void initialized(final ServletServerRequestWrapper servletServerRequestWrapper, final ServiceType serviceType) {
        Assert.requireNonNull(servletServerRequestWrapper, "servletServerRequestWrapper must not be null");
        Assert.requireNonNull(serviceType, "serviceType must not be null");

        if (isDebug) {
            logger.debug("Initialized servletRequestEvent. servletServerRequestWrapper={}, serviceType={}", servletServerRequestWrapper, serviceType);
        }

        final Trace trace = createTrace(servletServerRequestWrapper, serviceType);
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(ServiceType.SERVLET);
        recorder.recordApi(SERVLET_REQUEST_LISTENER_METHOD_DESCRIPTOR);
        if (this.isTraceRequestParam) {
            if (!excludeProfileMethodFilter.filter(servletServerRequestWrapper.getMethod())) {
                final String parameters = StringUtils.abbreviate(servletServerRequestWrapper.getParameters(), 512);
                if (StringUtils.hasLength(parameters)) {
                    recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                    if (isDebug) {
                        logger.debug("Record httpParam={}", parameters);
                    }
                }
            }
        }
    }

    private Trace createTrace(final ServletServerRequestWrapper servletServerRequestWrapper, final ServiceType serviceType) {
        final String requestURI = servletServerRequestWrapper.getRpcName();
        if (this.excludeUrlFilter.filter(requestURI)) {
            if (isTrace) {
                logger.trace("Filter requestURI={}", requestURI);
            }
            return null;
        }

        final Trace trace = this.requestTraceReader.read(servletServerRequestWrapper);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            // record root span
            recorder.recordServiceType(serviceType);
            recorder.recordApi(SERVLET_SYNC_METHOD_DESCRIPTOR);
            this.serverRequestRecorder.record(recorder, servletServerRequestWrapper);
            // record proxy HTTP header.
            this.proxyHttpHeaderRecorder.record(recorder, servletServerRequestWrapper);
        }
        return trace;
    }

    public void destroyed(final Throwable throwable, final int statusCode) {
        destroyed(throwable, statusCode, true);
    }

    public void destroyed(final Throwable throwable, final int statusCode, final boolean close) {
        if (isDebug) {
            logger.debug("Destroyed servletRequestEvent. throwable={}, statusCode={}", throwable, statusCode);
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
        } finally {
            trace.traceBlockEnd();
            if (close) {
                this.traceContext.removeTraceObject();
                trace.close();
            }
        }
    }
}