/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.okhttp.v2.interceptor;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.okhttp.v2.ConnectionGetter;
import com.navercorp.pinpoint.plugin.okhttp.EndPointUtils;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;
import com.navercorp.pinpoint.plugin.okhttp.v2.UserRequestGetter;
import com.navercorp.pinpoint.plugin.okhttp.v2.UserResponseGetter;
import com.squareup.okhttp.Request;

import java.net.URL;

/**
 * @author jaehong.kim
 */
public class HttpEngineSendRequestMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final InterceptorScope interceptorScope;

    private final boolean param;
    private final boolean cookie;
    private final DumpType cookieDumpType;
    private final SimpleSampler cookieSampler;

    public HttpEngineSendRequestMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorScope = interceptorScope;

        final OkHttpPluginConfig config = new OkHttpPluginConfig(traceContext.getProfilerConfig());
        this.param = config.isParam();
        this.cookie = config.isCookie();
        this.cookieDumpType = config.getCookieDumpType();
        if (cookie) {
            cookieSampler = SimpleSamplerFactory.createSampler(cookie, config.getCookieSamplingRate());
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

        if (!validate(target)) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        try {
            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            recorder.recordServiceType(OkHttpConstants.OK_HTTP_CLIENT);

            final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
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
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", OkHttpConstants.FIELD_USER_REQUEST);
            }
            return false;
        }

        if (!(target instanceof UserResponseGetter)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", OkHttpConstants.FIELD_USER_RESPONSE);
            }
            return false;
        }

        if (!(target instanceof ConnectionGetter)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", OkHttpConstants.FIELD_CONNECTION);
            }
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
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);

            // clear attachment.
            final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
            final Object attachment = getAttachment(invocation);
            if (attachment != null) {
                invocation.removeAttachment();
            }

            // typeCheck validate();
            final Request request = ((UserRequestGetter) target)._$PINPOINT$_getUserRequest();
            if (request != null) {
                try {
                    recorder.recordAttribute(AnnotationKey.HTTP_URL, InterceptorUtils.getHttpUrl(request.urlString(), param));
                    final String endpoint = getDestinationId(request.url());
                    recorder.recordDestinationId(endpoint);
                } catch (Exception ignored) {
                    logger.warn("Failed to invoke of request.url(). {}", ignored.getMessage());
                }
                recordRequest(trace, request, throwable);
            }

        } finally {
            trace.traceBlockEnd();
        }
    }

    private Object getAttachment(InterceptorScopeInvocation invocation) {
        if (invocation == null) {
            return null;
        }
        return invocation.getAttachment();
    }

    private String getDestinationId(URL httpUrl) {
        if (httpUrl == null || httpUrl.getHost() == null) {
            return "UnknownHttpClient";
        }
        final int port = EndPointUtils.getPort(httpUrl.getPort(), httpUrl.getDefaultPort());
        return HostAndPort.toHostAndPortString(httpUrl.getHost(), port);
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
        for (String cookie : request.headers("Cookie")) {
            if (cookieSampler.isSampling()) {
                final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.abbreviate(cookie, 1024));
            }

            return;
        }
    }
}