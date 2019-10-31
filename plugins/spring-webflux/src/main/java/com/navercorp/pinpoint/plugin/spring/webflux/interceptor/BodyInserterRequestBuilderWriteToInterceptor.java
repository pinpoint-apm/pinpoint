/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
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
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorderFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxPluginConfig;

import org.springframework.http.client.reactive.ClientHttpRequest;

import java.net.URI;

/**
 * @author jaehong.kim
 */
public class BodyInserterRequestBuilderWriteToInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final CookieRecorder<ClientHttpRequest> cookieRecorder;
    private final RequestTraceWriter<ClientHttpRequest> requestTraceWriter;

    public BodyInserterRequestBuilderWriteToInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);

        final SpringWebFluxPluginConfig config = new SpringWebFluxPluginConfig(traceContext.getProfilerConfig());
        final ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<ClientRequestWrapper>(config.isParam(), clientRequestAdaptor);

        final CookieExtractor<ClientHttpRequest> cookieExtractor = new ClientHttpRequestCookieExtractor();
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(config.getHttpDumpConfig(), cookieExtractor);

        final ClientHttpRequestClientHeaderAdaptor clientHeaderAdaptor = new ClientHttpRequestClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<ClientHttpRequest>(clientHeaderAdaptor, traceContext);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        if (!validate(args)) {
            return;
        }

        final ClientHttpRequest request = (ClientHttpRequest) args[0];
        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unexpected error, Current async trace is null");
            }
            return;
        }
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX_CLIENT);

        final URI url = request.getURI();
        String host = null;
        if (url != null) {
            host = HostAndPort.toHostAndPortString(url.getHost(), url.getPort());
        }
        requestTraceWriter.write(request, nextId, host);
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 1) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof ClientHttpRequest)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. Need ClientHttpRequest, args[0]={}.", args[0]);
            }
            return false;
        }

        return true;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        if (!validate(args)) {
            return;
        }

        final ClientHttpRequest request = (ClientHttpRequest) args[0];
        final ClientRequestWrapper clientRequestWrapper = new WebClientRequestWrapper(request);
        this.clientRequestRecorder.record(recorder, clientRequestWrapper, throwable);
        this.cookieRecorder.record(recorder, request, throwable);

        if (isAsync(result)) {
            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set closeable-AsyncContext {}", asyncContext);
            }
        }
    }

    private boolean isAsync(Object result) {
        if (result == null) {
            return false;
        }
        if (!(result instanceof AsyncContextAccessor)) {
            return false;
        }
        return true;
    }
}