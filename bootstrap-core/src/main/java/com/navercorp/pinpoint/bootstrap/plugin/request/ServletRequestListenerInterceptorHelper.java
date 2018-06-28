/*
 * Copyright 2018 NAVER Corp.
 *
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author jaehong.kim
 */
public class ServletRequestListenerInterceptorHelper {
    private static final ServletSyncMethodDescriptor SERVLET_SYNC_METHOD_DESCRIPTOR = new ServletSyncMethodDescriptor();
    private static final ServletRequestListenerMethodDescriptor SERVLET_REQUEST_LISTENER_METHOD_DESCRIPTOR = new ServletRequestListenerMethodDescriptor();

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final ServiceType serviceType;
    private final boolean isTraceRequestParam;
    private final Filter<String> excludeProfileMethodFilter;
    private final HttpStatusCodeRecorder httpStatusCodeRecorder;
    private final ServerRequestEntryPointInterceptorHelper interceptorHelper;

    public ServletRequestListenerInterceptorHelper(final TraceContext traceContext, final ServiceType serviceType, final Filter<String> excludeUrlFilter, final Filter<String> excludeProfileMethodFilter, final boolean isTraceRequestParam) {
        this.traceContext = traceContext;
        this.serviceType = serviceType;
        if (excludeProfileMethodFilter != null) {
            this.excludeProfileMethodFilter = excludeProfileMethodFilter;
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
        this.isTraceRequestParam = isTraceRequestParam;
        this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(traceContext.getProfilerConfig().getHttpStatusCodeErrors());
        this.interceptorHelper = new ServerRequestEntryPointInterceptorHelper(traceContext, excludeUrlFilter);

        this.traceContext.cacheApi(SERVLET_SYNC_METHOD_DESCRIPTOR);
        this.traceContext.cacheApi(SERVLET_REQUEST_LISTENER_METHOD_DESCRIPTOR);
    }

    public void initialized(final ServerRequestWrapper serverRequestWrapper) {
        Assert.requireNonNull(serverRequestWrapper, "serverRequestWrapper must not be null");

        if (isDebug) {
            logger.debug("Initialized servletRequestEvent. serverRequestWrapper={}", serverRequestWrapper);
        }

        final Trace trace = this.interceptorHelper.accept(serverRequestWrapper, this.serviceType, SERVLET_SYNC_METHOD_DESCRIPTOR);
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(ServiceType.SERVLET);
        if (this.isTraceRequestParam) {
            if (!excludeProfileMethodFilter.filter(serverRequestWrapper.getMethod())) {
                final String parameters = StringUtils.abbreviate(serverRequestWrapper.getParameters(), 512);
                if (StringUtils.hasLength(parameters)) {
                    recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                }
            }
        }
    }

    public void destroyed(final Throwable throwable, final int statusCode) {
        if (isDebug) {
            logger.debug("Destroyed servletRequestEvent. throwable={}", throwable);
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
            recorder.recordApi(SERVLET_REQUEST_LISTENER_METHOD_DESCRIPTOR);
            recorder.recordException(throwable);
            this.httpStatusCodeRecorder.record(trace.getSpanRecorder(), statusCode);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            this.traceContext.removeTraceObject();
            trace.traceBlockEnd();
            trace.close();
        }
    }
}