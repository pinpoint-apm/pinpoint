/*
 * Copyright 2021 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorderFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.vertx.HttpClientRequestClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.vertx.HttpClientRequestCookieExtractor;
import com.navercorp.pinpoint.plugin.vertx.HttpClientRequestWrapper;
import com.navercorp.pinpoint.plugin.vertx.SamplingRateFlag;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpClientConfig;
import io.vertx.core.http.HttpClientRequest;

public class PromiseImplInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final CookieRecorder<HttpClientRequest> cookieRecorder;
    private final RequestTraceWriter<HttpClientRequest> requestTraceWriter;

    public PromiseImplInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);

        final VertxHttpClientConfig config = new VertxHttpClientConfig(traceContext.getProfilerConfig());
        ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<>(config.isParam(), clientRequestAdaptor);

        CookieExtractor<HttpClientRequest> cookieExtractor = new HttpClientRequestCookieExtractor();
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(config.getHttpDumpConfig(), cookieExtractor);

        ClientHeaderAdaptor<HttpClientRequest> clientHeaderAdaptor = new HttpClientRequestClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        final HttpClientRequest request = (HttpClientRequest) args[0];
        String host = request.getHost();
        if (host == null) {
            host = "UNKNOWN";
        }

        // generate next trace id.
        final TraceId nextId = asyncContext.currentAsyncTraceObject().getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        requestTraceWriter.write(request, nextId, host);
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        if (isSamplingRateFalse(args)) {
            final HttpClientRequest request = (HttpClientRequest) args[0];
            this.requestTraceWriter.write(request);
            return null;
        }

        if (validate(args)) {
            return AsyncContextAccessorUtils.getAsyncContext(args, 0);
        }
        return null;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT);

        final HttpClientRequest request = (HttpClientRequest) args[0];
        String host = request.getHost();
        if (host == null) {
            host = "UNKNOWN";
        }

        final ClientRequestWrapper clientRequest = new HttpClientRequestWrapper(request, host);
        this.clientRequestRecorder.record(recorder, clientRequest, throwable);
        this.cookieRecorder.record(recorder, request, throwable);
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (isSamplingRateFalse(args)) {
            return null;
        }

        if (validate(args)) {
            return AsyncContextAccessorUtils.getAsyncContext(args, 0);
        }
        return null;
    }

    private boolean isSamplingRateFalse(final Object[] args) {
        if (ArrayUtils.getLength(args) < 1) {
            return false;
        }

        if (Boolean.FALSE == (args[0] instanceof SamplingRateFlag)) {
            return false;
        }
        final Boolean samplingRateFlag = ((SamplingRateFlag) args[0])._$PINPOINT$_getSamplingRateFlag();
        if (samplingRateFlag == null || Boolean.FALSE != samplingRateFlag) {
            return false;
        }

        if (Boolean.FALSE == (args[0] instanceof HttpClientRequest)) {
            return false;
        }

        return true;
    }

    private boolean validate(final Object[] args) {
        if (ArrayUtils.getLength(args) < 1) {
            logger.debug("Invalid args object. args={}.", args);
            return false;
        }

        if (!(args[0] instanceof AsyncContextAccessor)) {
            logger.debug("Invalid args[0] object. args[0]={}.", args[0]);
            return false;
        }

        if (!(args[0] instanceof HttpClientRequest)) {
            logger.debug("Invalid args[0] object. {}.", args[0]);
            return false;
        }

        return true;
    }
}
