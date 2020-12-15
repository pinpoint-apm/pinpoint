/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyPluginConfig;
import reactor.netty.http.client.HttpClientRequest;

/**
 * @author jaehong.kim
 */
public class HttpClientOperationsSendInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final RequestTraceWriter<HttpClientRequest> requestTraceWriter;

    public HttpClientOperationsSendInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);

        final ReactorNettyPluginConfig config = new ReactorNettyPluginConfig(traceContext.getProfilerConfig());
        final boolean param = config.isParam();
        final ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<ClientRequestWrapper>(param, clientRequestAdaptor);
        final HttpClientRequestHeaderAdaptor clientHeaderAdaptor = new HttpClientRequestHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<HttpClientRequest>(clientHeaderAdaptor, traceContext);
    }

    // BEFORE
    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        if (Boolean.FALSE == validate(target)) {
            return null;
        }

        final HttpClientRequest request = (HttpClientRequest) target;
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext == null) {
            // Set sampling rate to false
            this.requestTraceWriter.write(request);
            return null;
        }
        return asyncContext;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unexpected error, Current async trace is null");
            }
            return;
        }
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(ReactorNettyConstants.REACTOR_NETTY_CLIENT);

        final HttpClientRequest request = (HttpClientRequest) target;
        final ClientRequestWrapper clientRequestWrapper = new HttpClientRequestWrapper(request);
        this.requestTraceWriter.write(request, nextId, clientRequestWrapper.getDestinationId());
    }

    // AFTER
    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (Boolean.FALSE == validate(target)) {
            return null;
        }

        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        final HttpClientRequest request = (HttpClientRequest) target;
        final ClientRequestWrapper clientRequestWrapper = new HttpClientRequestWrapper(request);
        this.clientRequestRecorder.record(recorder, clientRequestWrapper, throwable);
    }

    private boolean validate(final Object target) {
        if (Boolean.FALSE == (target instanceof HttpClientRequest)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need ClientHttpRequest, target={}.", target);
            }
            return false;
        }

        return true;
    }
}
