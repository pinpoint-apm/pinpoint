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
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.method.ServletSyncMethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ServletRequestListener<REQ> {
    static final MethodDescriptor SERVLET_SYNC_METHOD_DESCRIPTOR = new ServletSyncMethodDescriptor();

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

    private final ProxyRequestRecorder<REQ> proxyRequestRecorder;

    private final boolean recordStatusCode;

    public ServletRequestListener(final ServiceType serviceType,
                                  final TraceContext traceContext,
                                  final RequestAdaptor<REQ> requestAdaptor,
                                  final RequestTraceReader<REQ> requestTraceReader,
                                  final Filter<String> excludeUrlFilter,
                                  final ParameterRecorder<REQ> parameterRecorder,
                                  final ProxyRequestRecorder<REQ> proxyRequestRecorder,
                                  final ServerRequestRecorder<REQ> serverRequestRecorder,
                                  final HttpStatusCodeRecorder httpStatusCodeRecorder,
                                  final boolean recordStatusCode) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.requestAdaptor = Objects.requireNonNull(requestAdaptor, "requestAdaptor");
        this.requestTraceReader = Objects.requireNonNull(requestTraceReader, "requestTraceReader");
        this.proxyRequestRecorder = Objects.requireNonNull(proxyRequestRecorder, "proxyRequestRecorder");
        this.excludeUrlFilter = Objects.requireNonNull(excludeUrlFilter, "excludeUrlFilter");
        this.parameterRecorder = Objects.requireNonNull(parameterRecorder, "parameterRecorder");
        this.serverRequestRecorder = Objects.requireNonNull(serverRequestRecorder, "serverRequestRecorder");
        this.httpStatusCodeRecorder = Objects.requireNonNull(httpStatusCodeRecorder, "httpStatusCodeRecorder");
        this.recordStatusCode = recordStatusCode;
        this.traceContext.cacheApi(SERVLET_SYNC_METHOD_DESCRIPTOR);
    }


    public void initialized(REQ request, final ServiceType serviceType, final MethodDescriptor methodDescriptor) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(serviceType, "serviceType");
        Objects.requireNonNull(methodDescriptor, "methodDescriptor");

        if (isDebug) {
            // An error may occur when the request variable is output to the log.
            logger.debug("Initialized requestEvent. serviceType={}, methodDescriptor={}", serviceType, methodDescriptor);
        }

        final Trace trace = createTrace(request);
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        if (trace.canSampled()) {
            recorder.recordServiceType(serviceType);
            recorder.recordApi(methodDescriptor);
        }
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

    /**
     * @param request    request
     * @param throwable  error
     * @param statusCode status code
     */
    public void destroyed(REQ request, final Throwable throwable, final int statusCode) {
        if (isDebug) {
            // An error may occur when the request variable is output to the log.
            logger.debug("Destroyed requestEvent. throwable={}, statusCode={}", throwable, statusCode);
        }

        final Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (this.recordStatusCode) {
            this.httpStatusCodeRecorder.record(trace.getSpanRecorder(), statusCode);
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (trace.canSampled()) {
                recorder.recordException(throwable);
                // Must be executed in destroyed()
                this.parameterRecorder.record(recorder, request, throwable);
            }
        } finally {
            trace.traceBlockEnd();
            this.traceContext.removeTraceObject();
            trace.close();
        }
    }
}