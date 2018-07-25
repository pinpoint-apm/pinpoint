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

package com.navercorp.pinpoint.plugin.okhttp.v3.interceptor;

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
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorderFactory;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;
import com.navercorp.pinpoint.plugin.okhttp.v3.OkHttpClientCookieExtractor;
import com.navercorp.pinpoint.plugin.okhttp.v3.OkHttpClientRequestAdaptor;
import com.navercorp.pinpoint.plugin.okhttp.v3.UserRequestGetter;
import com.navercorp.pinpoint.plugin.okhttp.v3.UserResponseGetter;
import okhttp3.Request;

/**
 * @author jaehong.kim
 */
public class HttpEngineSendRequestMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final InterceptorScope interceptorScope;
    private final ClientRequestRecorder<Request> clientRequestRecorder;
    private final CookieRecorder<Request> cookieRecorder;

    public HttpEngineSendRequestMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorScope = interceptorScope;

        final OkHttpPluginConfig config = new OkHttpPluginConfig(traceContext.getProfilerConfig());
        ClientRequestAdaptor<Request> clientRequestAdaptor = new OkHttpClientRequestAdaptor();
        this.clientRequestRecorder = new ClientRequestRecorder<Request>(config.isParam(), clientRequestAdaptor);

        CookieExtractor<Request> cookieExtractor = new OkHttpClientCookieExtractor();
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(config.getHttpDumpConfig(), cookieExtractor);

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
                this.clientRequestRecorder.record(recorder, request, throwable);
                this.cookieRecorder.record(recorder, request, throwable);
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
}