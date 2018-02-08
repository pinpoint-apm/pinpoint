/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jetty.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceReader;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestTrace;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.jetty.JettyConstants;
import com.navercorp.pinpoint.plugin.jetty.JettySyncMethodDescriptor;
import org.eclipse.jetty.server.Request;

import java.util.Enumeration;

/**
 * @author Chaein Jung
 */
public abstract class AbstractServerHandleInterceptor implements AroundInterceptor {
    public static final JettySyncMethodDescriptor JETTY_SYNC_API_TAG = new JettySyncMethodDescriptor();
    protected PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;
    private final Filter<String> excludeUrlFilter;
    private final ProxyHttpHeaderRecorder proxyHttpHeaderRecorder;
    private final ServerRequestRecorder serverRequestRecorder = new ServerRequestRecorder();
    private final RequestTraceReader requestTraceReader;

    public AbstractServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
        this.excludeUrlFilter = excludeFilter;
        this.proxyHttpHeaderRecorder = new ProxyHttpHeaderRecorder(traceContext.getProfilerConfig().isProxyHttpHeaderEnable());
        this.requestTraceReader = new RequestTraceReader(traceContext);

        traceContext.cacheApi(JETTY_SYNC_API_TAG);
    }

    abstract Request getRequest(Object[] args);
    abstract ServerRequestTrace getServerRequestTrace(Request request);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final Trace trace = createTrace(target, args);
            if (trace == null) {
                return;
            }
            // TODO STATDISABLE this logic was added to disable statistics tracing
            if (!trace.canSampled()) {
                return;
            }
            // ------------------------------------------------------
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(JettyConstants.JETTY_METHOD);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }


    private Trace createTrace(Object target, Object[] args) {
        final Request request = getRequest(args);

        final String requestURI = request.getRequestURI();
        if (excludeUrlFilter.filter(requestURI)) {
            if (isTrace) {
                logger.trace("filter requestURI:{}", requestURI);
            }
            return null;
        }

        final ServerRequestTrace serverRequestTrace = getServerRequestTrace(request);
        final Trace trace = this.requestTraceReader.read(serverRequestTrace);
        if (trace.canSampled()) {
            SpanRecorder recorder = trace.getSpanRecorder();
            // root
            recorder.recordServiceType(JettyConstants.JETTY);
            recorder.recordApi(JETTY_SYNC_API_TAG);
            this.serverRequestRecorder.record(recorder, serverRequestTrace);
            // record proxy HTTP headers.
            this.proxyHttpHeaderRecorder.record(recorder, serverRequestTrace);
        }
        return trace;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        // ------------------------------------------------------
        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            final Request request = getRequest(args);
            final String parameters = getRequestParameter(request, 64, 512);
            if (StringUtils.hasLength(parameters)) {
                recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
            }

            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after. Caused:{}", th.getMessage(), th);
            }
        } finally {
            traceContext.removeTraceObject();
            deleteTrace(trace, target, args, result, throwable);
        }
    }

    private String getRequestParameter(Request request, int eachLimit, int totalLimit) {
        Enumeration<?> attrs = request.getParameterNames();
        final StringBuilder params = new StringBuilder(64);
        while (attrs.hasMoreElements()) {
            if (params.length() != 0) {
                params.append('&');
            }
            // skip appending parameters if parameter size is bigger than totalLimit
            if (params.length() > totalLimit) {
                params.append("...");
                return params.toString();
            }
            String key = attrs.nextElement().toString();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append('=');
            Object value = request.getParameter(key);
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }
        return params.toString();
    }

    private void deleteTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.traceBlockEnd();
        trace.close();
    }
}