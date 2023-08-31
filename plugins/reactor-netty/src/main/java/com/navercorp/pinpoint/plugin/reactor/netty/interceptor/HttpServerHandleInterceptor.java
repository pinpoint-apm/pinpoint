/*
 * Copyright 2023 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextUtils;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerCookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListener;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServletResponseListener;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServletResponseListenerBuilder;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyPluginConfig;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.util.ConnectionObserverAdaptor;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * @author jaehong.kim
 */
public class HttpServerHandleInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final boolean enableAsyncEndPoint;
    private final ServletRequestListener<HttpServerRequest> servletRequestListener;
    private final ServletResponseListener<HttpServerResponse> servletResponseListener;
    private final ConnectionObserverAdaptor connectionObserver;

    public HttpServerHandleInterceptor(TraceContext traceContext,
                                       MethodDescriptor descriptor,
                                       RequestRecorderFactory<HttpServerRequest> requestRecorderFactory,
                                       int version) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;

        final ReactorNettyPluginConfig config = new ReactorNettyPluginConfig(traceContext.getProfilerConfig());
        RequestAdaptor<HttpServerRequest> requestAdaptor = new HttpRequestAdaptor();
        ParameterRecorder<HttpServerRequest> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());

        ServletRequestListenerBuilder<HttpServerRequest> reqBuilder = new ServletRequestListenerBuilder<>(ReactorNettyConstants.REACTOR_NETTY, traceContext, requestAdaptor);
        reqBuilder.setExcludeURLFilter(config.getExcludeUrlFilter());
        reqBuilder.setParameterRecorder(parameterRecorder);
        reqBuilder.setRequestRecorderFactory(requestRecorderFactory);

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        reqBuilder.setRealIpSupport(config.getRealIpHeader(), config.getRealIpEmptyValue());
        reqBuilder.setHttpStatusCodeRecorder(profilerConfig.getHttpStatusCodeErrors());
        reqBuilder.setServerHeaderRecorder(profilerConfig.readList(ServerHeaderRecorder.CONFIG_KEY_RECORD_REQ_HEADERS));
        reqBuilder.setServerCookieRecorder(profilerConfig.readList(ServerCookieRecorder.CONFIG_KEY_RECORD_REQ_COOKIES));
        reqBuilder.setRecordStatusCode(false);

        this.servletRequestListener = reqBuilder.build();
        this.servletResponseListener = new ServletResponseListenerBuilder<>(traceContext, new HttpResponseAdaptor()).build();
        this.enableAsyncEndPoint = config.isEnableAsyncEndPoint();
        this.connectionObserver = ConnectionObserverAdaptor.Factory.newAdaptor(version);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            if (connectionObserver.isReceived(args)) {
                beforeReceived(args);
            } else if (connectionObserver.isClosed(args)) {
                closed(args);
            }
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle", t);
        }
    }

    private void beforeReceived(final Object[] args) {
        final HttpServerRequest request = ArrayArgumentUtils.getArgument(args, 0, HttpServerRequest.class);
        if (request == null) {
            return;
        }
        final HttpServerResponse response = ArrayArgumentUtils.getArgument(args, 0, HttpServerResponse.class);
        if (response == null) {
            return;
        }

        this.servletRequestListener.initialized(request, ReactorNettyConstants.REACTOR_NETTY_INTERNAL, this.methodDescriptor);
        this.servletResponseListener.initialized(response, ReactorNettyConstants.REACTOR_NETTY_INTERNAL, this.methodDescriptor); //must after request listener due to trace block begin

        // Set end-point
        final Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        if (recorder != null) {
            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext(enableAsyncEndPoint);
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set asyncContext to args[0]. asyncContext={}", asyncContext);
            }
        }
    }

    private void closed(final Object[] args) {
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        if (asyncContext == null) {
            return;
        }
        // closed
        AsyncContextUtils.asyncStateFinish(asyncContext);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            if (connectionObserver.isReceived(args)) {
                afterReceived(args, throwable);
            }
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    private void afterReceived(Object[] args, Throwable throwable) {
        final HttpServerRequest request = (HttpServerRequest) args[0];
        final HttpServerResponse response = (HttpServerResponse) args[0];
        final int statusCode = getStatusCode(response);
        this.servletResponseListener.destroyed(response, throwable, statusCode); //must before request listener due to trace block ending
        this.servletRequestListener.destroyed(request, throwable, statusCode);
    }

    private int getStatusCode(final HttpServerResponse response) {
        try {
            HttpResponseStatus status = response.status();
            if (status != null) {
                return status.code();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}