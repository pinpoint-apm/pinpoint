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
package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceReader;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.vertx.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpHeaderFilter;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpServerConfig;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpServerMethodDescriptor;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ServerConnectionHandleRequestInterceptor implements AroundInterceptor {
    private static final String SCOPE_NAME = "##VERTX_SERVER_CONNECTION_TRACE";
    private static final VertxHttpServerMethodDescriptor VERTX_HTTP_SERVER_METHOD_DESCRIPTOR = new VertxHttpServerMethodDescriptor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final Filter<String> excludeUrlFilter;

    private final ProxyRequestRecorder<HttpServerRequest> proxyRequestRecorder;
    private final VertxHttpHeaderFilter httpHeaderFilter;
    private final ServerRequestRecorder<HttpServerRequest> serverRequestRecorder;
    private final RequestTraceReader<HttpServerRequest> requestTraceReader;
    private final ParameterRecorder<HttpServerRequest> parameterRecorder;

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    private final HttpStatusCodeRecorder httpStatusCodeRecorder;

    public ServerConnectionHandleRequestInterceptor(final TraceContext traceContext,
                                                    final MethodDescriptor methodDescriptor,
                                                    final RequestRecorderFactory<HttpServerRequest> requestRecorderFactory) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.descriptor = methodDescriptor;

        final VertxHttpServerConfig config = new VertxHttpServerConfig(traceContext.getProfilerConfig());
        this.excludeUrlFilter = config.getExcludeUrlFilter();

        RequestAdaptor<HttpServerRequest> requestAdaptor = new HttpServerRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, config.getRealIpHeader(), config.getRealIpEmptyValue());
        this.parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());
        this.proxyRequestRecorder = requestRecorderFactory.getProxyRequestRecorder(requestAdaptor);
        this.httpHeaderFilter = new VertxHttpHeaderFilter(config.isHidePinpointHeader());
        this.serverRequestRecorder = new ServerRequestRecorder<>(requestAdaptor);
        this.requestTraceReader = new RequestTraceReader<>(traceContext, requestAdaptor, true);
        traceContext.cacheApi(VERTX_HTTP_SERVER_METHOD_DESCRIPTOR);

        this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(traceContext.getProfilerConfig().getHttpStatusCodeErrors());
    }


    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (currentTrace() != null) {
            // duplicate trace.
            return;
        }

        try {
            if (!validate(args)) {
                // invalid args.
                return;
            }

            final HttpServerRequest request = (HttpServerRequest) args[0];
            final HttpServerResponse response = request.response();
            if (!(response instanceof AsyncContextAccessor)) {
                return;
            }

            // create trace for standalone entry point.
            final Trace trace = createTrace(request);
            if (trace == null) {
                return;
            }

            entryScope(trace);
            this.httpHeaderFilter.filter(request);
            // for URI-metric
//            if (!trace.canSampled()) {
//                return;
//            }

            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(VertxConstants.VERTX_HTTP_SERVER_INTERNAL);

            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext(true);
            ((AsyncContextAccessor) request)._$PINPOINT$_setAsyncContext(asyncContext);
            ((AsyncContextAccessor) response)._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set asyncContext to request/response. asyncContext={}", asyncContext);
            }

        } catch (Throwable t) {
            logger.warn("BEFORE. Caused:{}", t.getMessage(), t);
        }
    }

    private Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    private boolean validate(final Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return false;
        }

        Object arg = args[0];
        if (!(arg instanceof HttpServerRequest)) {
            return false;
        }

        if (!(arg instanceof AsyncContextAccessor)) {
            return false;
        }

        return true;
    }


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = currentTrace();
        if (trace == null) {
            return;
        }

        if (!hasScope(trace)) {
            // not vertx trace.
            return;
        }

        if (!leaveScope(trace)) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to leave scope. trace={}, sampled={}", trace, trace.canSampled());
            }
            // delete unstable trace.
            deleteTrace(trace);
            return;
        }

        if (!isEndScope(trace)) {
            // ignored recursive call.
            return;
        }

        final boolean validate = validate(args);
        if (validate) {
            final HttpServerRequest request = (HttpServerRequest) args[0];
            HttpServerResponse response = request.response();
            this.httpStatusCodeRecorder.record(trace.getSpanRecorder(), response.getStatusCode());
        }

        // for URI-metric
//        if (!trace.canSampled()) {
//            deleteTrace(trace);
//            return;
//        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            if (validate) {
                final HttpServerRequest request = (HttpServerRequest) args[0];
                parameterRecorder.record(recorder, request, throwable);
            }
        } catch (Throwable t) {
            logger.warn("AFTER. Caused:{}", t.getMessage(), t);
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
    }


    private Trace createTrace(final HttpServerRequest request) {
        final String requestURI = request.path();
        if (requestURI != null && excludeUrlFilter.filter(requestURI)) {
            // skip request.
            if (isTrace) {
                logger.trace("filter requestURI:{}", requestURI);
            }
            return null;
        }

        final Trace trace = this.requestTraceReader.read(request);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            // root
            recorder.recordServiceType(VertxConstants.VERTX_HTTP_SERVER);
            recorder.recordApi(VERTX_HTTP_SERVER_METHOD_DESCRIPTOR);
            this.serverRequestRecorder.record(recorder, request);
            // record proxy HTTP header.
            this.proxyRequestRecorder.record(recorder, request);
        }

        if (!initScope(trace)) {
            // invalid scope.
            deleteTrace(trace);
            return null;
        }
        return trace;
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }


    private boolean initScope(final Trace trace) {
        // add user scope.
        final TraceScope oldScope = trace.addScope(SCOPE_NAME);
        if (oldScope != null) {
            // delete corrupted trace.
            if (logger.isInfoEnabled()) {
                logger.info("Duplicated trace scope={}.", oldScope.getName());
            }
            return false;
        }

        return true;
    }

    private void entryScope(final Trace trace) {
        ScopeUtils.entryScope(trace, SCOPE_NAME);
        if (isDebug) {
            logger.debug("Try enter trace scope={}", SCOPE_NAME);
        }
    }

    private boolean leaveScope(final Trace trace) {
        if (ScopeUtils.leaveScope(trace, SCOPE_NAME)) {
            if (isDebug) {
                logger.debug("Leave trace scope={}", SCOPE_NAME);
            }
            return true;
        }
        return false;
    }

    private boolean hasScope(final Trace trace) {
        return ScopeUtils.hasScope(trace, SCOPE_NAME);
    }

    private boolean isEndScope(final Trace trace) {
        return ScopeUtils.isEndScope(trace, SCOPE_NAME);
    }
}