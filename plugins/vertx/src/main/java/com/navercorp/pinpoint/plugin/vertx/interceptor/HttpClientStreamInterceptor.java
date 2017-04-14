/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpClientConfig;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author jaehong.kim
 */
public class HttpClientStreamInterceptor implements AroundInterceptor {
    private static final int MAX_READ_SIZE = 1024;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private boolean param;
    private final boolean cookie;
    private final DumpType cookieDumpType;
    private final SimpleSampler cookieSampler;

    private TraceContext traceContext;
    private MethodDescriptor descriptor;

    public HttpClientStreamInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;

        final VertxHttpClientConfig config = new VertxHttpClientConfig(traceContext.getProfilerConfig());
        this.param = config.isParam();
        this.cookie = config.isCookie();
        this.cookieDumpType = config.getCookieDumpType();
        if (cookie) {
            this.cookieSampler = SimpleSamplerFactory.createSampler(cookie, config.getCookieSamplingRate());
        } else {
            this.cookieSampler = null;
        }
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            if (!validate(args)) {
                return;
            }

            final HttpRequest request = (HttpRequest) args[0];
            final HttpHeaders headers = request.headers();
            if (headers == null) {
                // defense code.
                return;
            }

            final String uri = request.uri();
            final String host = (String) args[1];

            // generate next trace id.
            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());

            headers.add(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            headers.add(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
            headers.add(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
            headers.add(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            headers.add(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            headers.add(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));

            if (host != null) {
                headers.add(Header.HTTP_HOST.toString(), host);
                recorder.recordDestinationId(host);
            } else {
                recorder.recordDestinationId("unknown");
            }

            if (uri != null) {
                final String httpUrl = InterceptorUtils.getHttpUrl(uri, param);
                recorder.recordAttribute(AnnotationKey.HTTP_URL, httpUrl);
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", t.getMessage(), t);
            }
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 2) {
            logger.debug("Invalid args object. args={}.", args);
            return false;
        }

        if (!(args[0] instanceof HttpRequest)) {
            logger.debug("Invalid args[0] object. {}.", args[0]);
            return false;
        }

        if (!(args[1] instanceof String)) {
            logger.debug("Invalid args[1] object. {}.", args[1]);
            return false;
        }

        return true;
    }


    private void recordCookie(final HttpHeaders headers, final Trace trace) {
        final String cookie = headers.get("Cookie");
        if (cookie == null) {
            return;
        }

        if (this.cookieSampler.isSampling()) {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.abbreviate(cookie, MAX_READ_SIZE));
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT);

            if (!validate(args)) {
                return;
            }

            final HttpRequest request = (HttpRequest) args[0];
            final HttpHeaders headers = request.headers();
            if (headers == null) {
                return;
            }

            final boolean isException = InterceptorUtils.isThrowable(throwable);
            if (cookie) {
                if (DumpType.ALWAYS == cookieDumpType) {
                    recordCookie(headers, trace);
                } else if (DumpType.EXCEPTION == cookieDumpType && isException) {
                    recordCookie(headers, trace);
                }
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}