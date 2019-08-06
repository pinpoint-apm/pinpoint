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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import org.apache.http.HttpRequest;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;
import com.navercorp.pinpoint.plugin.httpclient4.HttpCallContext;
import com.navercorp.pinpoint.plugin.httpclient4.HttpCallContextFactory;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

/**
 * @author minwoo.jung
 * @author jaehong.kim
 */
public abstract class AbstractHttpClientExecuteMethodInterceptor implements AroundInterceptor {
    protected final PLogger logger;
    protected final boolean isDebug;

    private final boolean isHasCallbackParam;
    protected final TraceContext traceContext;
    protected final MethodDescriptor descriptor;
    protected final InterceptorScope interceptorScope;

    public AbstractHttpClientExecuteMethodInterceptor(Class<? extends AbstractHttpClientExecuteMethodInterceptor> childClazz, boolean isHasCallbackParam, TraceContext context, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        this.logger = PLoggerFactory.getLogger(childClazz);
        this.isDebug = logger.isDebugEnabled();

        this.traceContext = context;
        this.descriptor = methodDescriptor;
        this.isHasCallbackParam = isHasCallbackParam;
        this.interceptorScope = interceptorScope;
    }

    abstract NameIntValuePair<String> getHost(Object[] args);

    abstract HttpRequest getHttpRequest(Object[] args);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(HttpClient4Constants.HTTP_CLIENT_4_INTERNAL);
        final NameIntValuePair<String> host = getHost(args);
        if (host != null) {
            final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
            if (invocation != null) {
                final HttpCallContext callContext = (HttpCallContext) invocation.getOrCreateAttachment(HttpCallContextFactory.HTTPCALL_CONTEXT_FACTORY);
                callContext.setHost(host.getName());
                callContext.setPort(host.getValue());
            }
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
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);

            final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
            if (invocation != null) {
                // clear
                invocation.removeAttachment();
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}