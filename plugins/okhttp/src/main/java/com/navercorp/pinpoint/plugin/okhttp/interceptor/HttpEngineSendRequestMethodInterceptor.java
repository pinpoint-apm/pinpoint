/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.okhttp.interceptor;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Group;
import com.navercorp.pinpoint.bootstrap.interceptor.group.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.okhttp.*;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;

/**
 * @author jaehong.kim
 */
@Group(OkHttpConstants.SEND_REQUEST_SCOPE)
public class HttpEngineSendRequestMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;
    private InterceptorGroup interceptorGroup;

    private final boolean cookie;
    private final DumpType cookieDumpType;
    private final SimpleSampler cookieSampler;
    private final boolean statusCode;

    public HttpEngineSendRequestMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorGroup interceptorGroup, OkHttpPluginConfig config) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorGroup = interceptorGroup;

        this.cookie = config.isCookie();
        this.cookieDumpType = config.getCookieDumpType();
        if(cookie) {
            cookieSampler = SimpleSamplerFactory.createSampler(cookie, config.getCookieSamplingRate());
        } else {
            this.cookieSampler = null;
        }

        statusCode = config.isStatusCode();
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

        if (!validate(target)) {
            return;
        }

        if(!trace.canSampled()) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        try {
            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            recorder.recordServiceType(OkHttpConstants.OK_HTTP_CLIENT);

            InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
            if (invocation != null) {
                invocation.getOrCreateAttachment(new AttachmentFactory() {
                    @Override
                    public Object createAttachment() {
                        return nextId;
                    }
                });
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(Object target) {
        if (!(target instanceof UserRequestGetter)) {
            logger.debug("Invalid target object. Need field accessor({}).", OkHttpConstants.FIELD_USER_REQUEST);
            return false;
        }

        if (!(target instanceof UserResponseGetter)) {
            logger.debug("Invalid target object. Need field accessor({}).", OkHttpConstants.FIELD_USER_RESPONSE);
            return false;
        }

        if (!(target instanceof ConnectionGetter)) {
            logger.debug("Invalid target object. Need field accessor({}).", OkHttpConstants.FIELD_CONNECTION);
            return false;
        }

        return true;
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

        if (!validate(target)) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);

            Request request = ((UserRequestGetter) target)._$PINPOINT$_getUserRequest();
            if (request != null) {
                recorder.recordAttribute(AnnotationKey.HTTP_URL, request.httpUrl().toString());
                final String endpoint = getDestinationId(request.httpUrl());
                recorder.recordDestinationId(endpoint);
                recordRequest(trace, request, throwable);
            }

            // clear attachment.
            InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
            if(invocation != null && invocation.getAttachment() != null) {
                invocation.removeAttachment();
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getDestinationId(HttpUrl httpUrl) {
        if (httpUrl == null || httpUrl.host() == null) {
            return "UnknownHttpClient";
        }
        if (httpUrl.port() == HttpUrl.defaultPort(httpUrl.scheme())) {
            return httpUrl.host();
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(httpUrl.host());
        sb.append(':');
        sb.append(httpUrl.port());
        return sb.toString();
    }

    private void recordRequest(Trace trace, Request request, Throwable throwable) {
        final boolean isException = InterceptorUtils.isThrowable(throwable);
        if (cookie) {
            if (DumpType.ALWAYS == cookieDumpType) {
                recordCookie(request, trace);
            } else if (DumpType.EXCEPTION == cookieDumpType && isException) {
                recordCookie(request, trace);
            }
        }
    }

    private void recordCookie(Request request, Trace trace) {
        for(String cookie : request.headers("Cookie")) {
            if(cookieSampler.isSampling()) {
                final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.drop(cookie, 1024));
            }

            return;
        }
    }
}