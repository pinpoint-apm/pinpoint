/*
 * Copyright 2016 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceReader;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestEntryPointInterceptorHelper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpHeaderFilter;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpServerConfig;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpServerMethodDescriptor;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpServerRequestWrapperFactory;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

/**
 * @author jaehong.kim
 */
public class ServerConnectionHandleRequestInterceptor implements AroundInterceptor {
    private static final String SCOPE_NAME = "##VERTX_SERVER_CONNECTION_TRACE";
    private static final VertxHttpServerMethodDescriptor VERTX_HTTP_SERVER_METHOD_DESCRIPTOR = new VertxHttpServerMethodDescriptor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final boolean isTraceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> excludeProfileMethodFilter;
    private final ProxyHttpHeaderRecorder proxyHttpHeaderRecorder;
    private final VertxHttpHeaderFilter httpHeaderFilter;
    private final ServerRequestRecorder serverRequestRecorder = new ServerRequestRecorder();
    private final RequestTraceReader requestTraceReader;
    private final VertxHttpServerRequestWrapperFactory serverRequestWrapperFactory;
    private final ServerRequestEntryPointInterceptorHelper entryPointInterceptorHelper;

    private TraceContext traceContext;
    private MethodDescriptor descriptor;

    public ServerConnectionHandleRequestInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;

        final VertxHttpServerConfig config = new VertxHttpServerConfig(traceContext.getProfilerConfig());
        this.excludeUrlFilter = config.getExcludeUrlFilter();
        this.isTraceRequestParam = config.isTraceRequestParam();
        this.excludeProfileMethodFilter = config.getExcludeProfileMethodFilter();
        this.proxyHttpHeaderRecorder = new ProxyHttpHeaderRecorder(traceContext.getProfilerConfig().isProxyHttpHeaderEnable());
        this.httpHeaderFilter = new VertxHttpHeaderFilter(config.isHidePinpointHeader());
        this.requestTraceReader = new RequestTraceReader(traceContext, true);
        this.serverRequestWrapperFactory = new VertxHttpServerRequestWrapperFactory(config.getRealIpHeader(), config.getRealIpEmptyValue());
        this.entryPointInterceptorHelper = new ServerRequestEntryPointInterceptorHelper(traceContext, excludeUrlFilter);

        traceContext.cacheApi(VERTX_HTTP_SERVER_METHOD_DESCRIPTOR);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (traceContext.currentRawTraceObject() != null) {
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
                if (isDebug) {
                    logger.debug("Invalid response. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
                }
                return;
            }

            // create trace for standalone entry point.
            final ServerRequestWrapper serverRequestWrapper = this.serverRequestWrapperFactory.get(request);
            final Trace trace = this.entryPointInterceptorHelper.accept(serverRequestWrapper, VertxConstants.VERTX_HTTP_SERVER_INTERNAL, this.descriptor);
            if (trace == null) {
                return;
            }

            if (!initScope(trace)) {
                // invalid scope.
                deleteTrace(trace);
                return;
            }
            entryScope(trace);
            // Hide pinpoint headers
            this.httpHeaderFilter.filter(request);

            if (!trace.canSampled()) {
                return;
            }

            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(VertxConstants.VERTX_HTTP_SERVER_INTERNAL);
            if (this.isTraceRequestParam) {
                if (!excludeProfileMethodFilter.filter(serverRequestWrapper.getMethod())) {
                    final String parameters = serverRequestWrapper.getParameters();
                    if (parameters != null && !parameters.isEmpty()) {
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                    }
                }
            }

            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext(true);
            ((AsyncContextAccessor) request)._$PINPOINT$_setAsyncContext(asyncContext);
            ((AsyncContextAccessor) response)._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set closeable-AsyncContext {}", asyncContext);
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", t.getMessage(), t);
            }
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 1) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof HttpServerRequest)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. {}.", args[0]);
            }
            return false;
        }

        if (!(args[0] instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
            }
            return false;
        }

        return true;
    }


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
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

        if (!trace.canSampled()) {
            deleteTrace(trace);
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
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
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            scope.tryEnter();
            if (isDebug) {
                logger.debug("Try enter trace scope={}", scope.getName());
            }
        }
    }

    private boolean leaveScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
                if (isDebug) {
                    logger.debug("Leave trace scope={}", scope.getName());
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean hasScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null;
    }

    private boolean isEndScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null && !scope.isActive();
    }
}