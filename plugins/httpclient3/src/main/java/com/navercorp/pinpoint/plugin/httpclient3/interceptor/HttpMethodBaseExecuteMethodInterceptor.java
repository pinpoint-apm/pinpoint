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

package com.navercorp.pinpoint.plugin.httpclient3.interceptor;

import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3RequestWrapper;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3CallContext;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3CallContextFactory;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3Constants;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3PluginConfig;

/**
 * @author Minwoo Jung
 * @author jaehong.kim
 */
public class HttpMethodBaseExecuteMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope interceptorScope;
    private ClientRequestRecorder clientRequestRecorder;

    private final boolean io;

    public HttpMethodBaseExecuteMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (methodDescriptor == null) {
            throw new NullPointerException("methodDescriptor must not be null");
        }
        if (interceptorScope == null) {
            throw new NullPointerException("interceptorScope must not be null");
        }
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        this.interceptorScope = interceptorScope;

        final HttpClient3PluginConfig config = new HttpClient3PluginConfig(traceContext.getProfilerConfig());
        final boolean param = config.isParam();
        final HttpDumpConfig httpDumpConfig = config.getHttpDumpConfig();
        this.clientRequestRecorder = new ClientRequestRecorder(param, httpDumpConfig);

        this.io = config.isIo();
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

        if (!trace.canSampled()) {
            if (target instanceof HttpMethod) {
                final HttpMethod httpMethod = (HttpMethod) target;
                final HttpConnection httpConnection = getHttpConnection(args);
                final RequestTraceWriter requestTraceWriter = new RequestTraceWriter(new HttpClient3RequestWrapper(httpMethod, httpConnection));
                requestTraceWriter.write();
            }
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        // generate next trace id.
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(HttpClient3Constants.HTTP_CLIENT_3);
        // set http header for trace.
        if (target instanceof HttpMethod) {
            final HttpMethod httpMethod = (HttpMethod) target;
            final HttpConnection httpConnection = getHttpConnection(args);
            final RequestTraceWriter requestTraceWriter = new RequestTraceWriter(new HttpClient3RequestWrapper(httpMethod, httpConnection));
            requestTraceWriter.write(nextId, this.traceContext.getApplicationName(), this.traceContext.getServerTypeCode(), this.traceContext.getProfilerConfig().getApplicationNamespace());
        }

        // init attachment for io(read/write).
        initAttachment();
    }

    private HttpConnection getHttpConnection(final Object[] args) {
        if (args != null && args.length > 1 && args[1] instanceof HttpConnection) {
            return (HttpConnection) args[1];
        }
        return null;
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
            if (target instanceof HttpMethod) {
                final HttpMethod httpMethod = (HttpMethod) target;
                final HttpConnection httpConnection = getHttpConnection(args);
                this.clientRequestRecorder.record(recorder, new HttpClient3RequestWrapper(httpMethod, httpConnection), throwable);
            }

            if (result != null) {
                recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, result);
            }

            final HttpClient3CallContext callContext = getAndCleanAttachment();
            if (callContext != null) {
                recordIo(recorder, callContext);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private void initAttachment() {
        InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        if (invocation != null) {
            invocation.getOrCreateAttachment(HttpClient3CallContextFactory.HTTPCLIENT3_CONTEXT_FACTORY);
        }
    }

    private HttpClient3CallContext getAndCleanAttachment() {
        final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        final Object attachment = getAttachment(invocation);
        if (attachment instanceof HttpClient3CallContext) {
            return (HttpClient3CallContext) invocation.removeAttachment();
        }

        return null;
    }

    private Object getAttachment(InterceptorScopeInvocation invocation) {
        if (invocation == null) {
            return null;
        }
        return invocation.getAttachment();
    }

    private void recordIo(SpanEventRecorder recorder, HttpClient3CallContext callContext) {
        if (io) {
            IntBooleanIntBooleanValue value = new IntBooleanIntBooleanValue((int) callContext.getWriteElapsedTime(), callContext.isWriteFail(), (int) callContext.getReadElapsedTime(), callContext.isReadFail());
            recorder.recordAttribute(AnnotationKey.HTTP_IO, value);
        }
    }
}