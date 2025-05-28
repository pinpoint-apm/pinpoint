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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;
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
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityRecorderFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4CookieExtractor;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4EntityExtractor;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4PluginConfig;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4RequestWrapper;
import com.navercorp.pinpoint.plugin.httpclient4.HttpRequest4ClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.httpclient4.RequestProducerGetter;
import com.navercorp.pinpoint.plugin.httpclient4.ResultFutureGetter;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

/**
 * @author minwoo.jung
 * @author jaehong.kim
 */
public class DefaultClientExchangeHandlerImplStartMethodInterceptor extends SpanEventBlockSimpleAroundInterceptorForPlugin {
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final CookieRecorder<HttpRequest> cookieRecorder;
    private final EntityRecorder<HttpRequest> entityRecorder;

    private final RequestTraceWriter<HttpRequest> requestTraceWriter;
    private final boolean markError;

    public DefaultClientExchangeHandlerImplStartMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);

        final boolean param = HttpClient4PluginConfig.isParam(traceContext.getProfilerConfig());
        final HttpDumpConfig httpDumpConfig = HttpClient4PluginConfig.getHttpDumpConfig(traceContext.getProfilerConfig());

        ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<>(param, clientRequestAdaptor);

        CookieExtractor<HttpRequest> cookieExtractor = HttpClient4CookieExtractor.INSTANCE;
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(httpDumpConfig, cookieExtractor);

        EntityExtractor<HttpRequest> entityExtractor = HttpClient4EntityExtractor.INSTANCE;
        this.entityRecorder = EntityRecorderFactory.newEntityRecorder(httpDumpConfig, entityExtractor);

        ClientHeaderAdaptor<HttpRequest> clientHeaderAdaptor = new HttpRequest4ClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
        this.markError = HttpClient4PluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public boolean checkBeforeTraceBlockBegin(Trace trace, Object target, Object[] args) {
        final HttpRequest httpRequest = getHttpRequest(target);
        if (httpRequest == null) {
            return false;
        }

        if (requestTraceWriter.isNested(httpRequest)) {
            return false;
        }

        if (Boolean.FALSE == trace.canSampled()) {
            this.requestTraceWriter.write(httpRequest);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args) {
        final HttpRequest httpRequest = getHttpRequest(target);
        if (httpRequest == null) {
            return;
        }
        final NameIntValuePair<String> host = getHost(target);
        // set remote trace
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        final String hostString = getHostString(host.getName(), host.getValue());
        this.requestTraceWriter.write(httpRequest, nextId, hostString);

        if (isAsynchronousInvocation(target, args)) {
            // set asynchronous trace
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            // check type isAsynchronousInvocation()
            ((AsyncContextAccessor) ((ResultFutureGetter) target)._$PINPOINT$_getResultFuture())._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set AsyncContext {}", asyncContext);
            }
        }
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
    }

    private String getHostString(String hostName, int port) {
        if (hostName != null) {
            return HostAndPort.toHostAndPortString(hostName, port);
        }
        return null;
    }

    private HttpRequest getHttpRequest(final Object target) {
        try {
            if (!(target instanceof RequestProducerGetter)) {
                return null;
            }
            final HttpAsyncRequestProducer requestProducer = ((RequestProducerGetter) target)._$PINPOINT$_getRequestProducer();
            return requestProducer.generateRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private NameIntValuePair<String> getHost(final Object target) {
        if (target instanceof RequestProducerGetter) {
            final HttpAsyncRequestProducer producer = ((RequestProducerGetter) target)._$PINPOINT$_getRequestProducer();
            final HttpHost httpHost = producer.getTarget();
            if (httpHost != null) {
                return new NameIntValuePair<>(httpHost.getHostName(), httpHost.getPort());
            }
        }
        return new NameIntValuePair<>(null, -1);
    }

    private boolean isAsynchronousInvocation(final Object target, final Object[] args) {
        if (!(target instanceof ResultFutureGetter)) {
            logger.debug("Invalid target object. Need field accessor({}).", HttpClient4Constants.FIELD_RESULT_FUTURE);
            return false;
        }

        BasicFuture<?> future = ((ResultFutureGetter) target)._$PINPOINT$_getResultFuture();
        if (future == null) {
            logger.debug("Invalid target object. field is null({}).", HttpClient4Constants.FIELD_RESULT_FUTURE);
            return false;
        }

        if (!(future instanceof AsyncContextAccessor)) {
            logger.debug("Invalid resultFuture field object. Need metadata accessor({}).", HttpClient4Constants.METADATA_ASYNC_CONTEXT);
            return false;
        }

        return true;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordServiceType(HttpClient4Constants.HTTP_CLIENT_4);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(markError, throwable);

        final HttpRequest httpRequest = getHttpRequest(target);
        final NameIntValuePair<String> host = getHost(target);
        if (httpRequest != null) {
            // Accessing httpRequest here not BEFORE() because it can cause side effect.
            ClientRequestWrapper clientRequest = new HttpClient4RequestWrapper(httpRequest, host.getName(), host.getValue());
            this.clientRequestRecorder.record(recorder, clientRequest, throwable);
            this.cookieRecorder.record(recorder, httpRequest, throwable);
            this.entityRecorder.record(recorder, httpRequest, throwable);
        }
    }
}