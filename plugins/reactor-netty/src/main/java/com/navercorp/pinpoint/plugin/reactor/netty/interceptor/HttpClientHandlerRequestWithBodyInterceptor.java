/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
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
public class HttpClientHandlerRequestWithBodyInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final RequestTraceWriter<HttpClientRequest> requestTraceWriter;

    public HttpClientHandlerRequestWithBodyInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
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
        if (Boolean.FALSE == validate(args)) {
            return null;
        }

        final HttpClientRequest request = (HttpClientRequest) args[0];
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

        final HttpClientRequest request = (HttpClientRequest) args[0];
        final ClientRequestWrapper clientRequestWrapper = new HttpClientRequestWrapper(request);
        this.requestTraceWriter.write(request, nextId, clientRequestWrapper.getDestinationId());

        // Set HttpClientOptions
        if (request instanceof AsyncContextAccessor) {
            ((AsyncContextAccessor) request)._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    // AFTER
    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (Boolean.FALSE == validate(args)) {
            return null;
        }

        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        final HttpClientRequest request = (HttpClientRequest) args[0];
        final ClientRequestWrapper clientRequestWrapper = new HttpClientRequestWrapper(request);
        this.clientRequestRecorder.record(recorder, clientRequestWrapper, throwable);
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 1) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof HttpClientRequest)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. Need ClientHttpRequest, args[0]={}.", args[0]);
            }
            return false;
        }

        return true;
    }
}
