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
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderHandler;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpServerConfig;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpServerMethodDescriptor;
import io.vertx.core.http.impl.HttpServerRequestImpl;
import io.vertx.core.http.impl.HttpServerResponseImpl;

import java.util.Map;

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
    private final RemoteAddressResolver<HttpServerRequestImpl> remoteAddressResolver;
    private final ProxyHttpHeaderRecorder proxyHttpHeaderRecorder;

    private TraceContext traceContext;
    private MethodDescriptor descriptor;

    public ServerConnectionHandleRequestInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;

        final VertxHttpServerConfig config = new VertxHttpServerConfig(traceContext.getProfilerConfig());
        this.excludeUrlFilter = config.getExcludeUrlFilter();
        final String proxyIpHeader = config.getRealIpHeader();
        if (proxyIpHeader == null || proxyIpHeader.isEmpty()) {
            this.remoteAddressResolver = new Bypass<HttpServerRequestImpl>();
        } else {
            final String tomcatRealIpEmptyValue = config.getRealIpEmptyValue();
            this.remoteAddressResolver = new RealIpHeaderResolver<HttpServerRequestImpl>(proxyIpHeader, tomcatRealIpEmptyValue);
        }
        this.isTraceRequestParam = config.isTraceRequestParam();
        this.excludeProfileMethodFilter = config.getExcludeProfileMethodFilter();
        this.proxyHttpHeaderRecorder = new ProxyHttpHeaderRecorder(traceContext);

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

            final HttpServerRequestImpl request = (HttpServerRequestImpl) args[0];
            final HttpServerResponseImpl response = (HttpServerResponseImpl) args[1];

            // create trace for standalone entry point.
            final Trace trace = createTrace(request);
            if (trace == null) {
                return;
            }

            entryScope(trace);

            if (!trace.canSampled()) {
                return;
            }

            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(VertxConstants.VERTX_HTTP_SERVER_INTERNAL);

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
        if (args == null || args.length < 2) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof HttpServerRequestImpl)) {
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


        if (!(args[1] instanceof HttpServerResponseImpl)) {
            if (isDebug) {
                logger.debug("Invalid args[1] object. {}.", args[1]);
            }
            return false;
        }

        if (!(args[1] instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[1] object. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
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
            if (validate(args)) {
                if (this.isTraceRequestParam) {
                    final HttpServerRequestImpl request = (HttpServerRequestImpl) args[0];
                    if (!excludeProfileMethodFilter.filter(request.method().toString())) {
                        final String parameters = getRequestParameter(request, 64, 512);
                        if (parameters != null && !parameters.isEmpty()) {
                            recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
    }

    private Trace createTrace(final HttpServerRequestImpl request) {
        final String requestURI = request.path();
        if (requestURI != null && excludeUrlFilter.filter(requestURI)) {
            // skip request.
            if (isTrace) {
                logger.trace("filter requestURI:{}", requestURI);
            }
            return null;
        }

        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("Remote call sampling flag found. skip trace requestUrl:{}, remoteAddr:{}", request.path(), request.remoteAddress());
            }
            if (!initScope(trace)) {
                // invalid scope.
                deleteTrace(trace);
                return null;
            }

            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            final Trace trace = traceContext.continueAsyncTraceObject(traceId);
            if (trace.canSampled()) {
                final SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, request.path(), request.remoteAddress());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, request.path(), request.remoteAddress());
                }
            }
            if (!initScope(trace)) {
                // invalid scope.
                deleteTrace(trace);
                return null;
            }

            return trace;
        } else {
            // make asynchronous trace.
            final Trace trace = traceContext.newAsyncTraceObject();
            if (trace.canSampled()) {
                final SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", request.path(), request.remoteAddress());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", request.path(), request.remoteAddress());
                }
            }

            if (!initScope(trace)) {
                // invalid scope.
                deleteTrace(trace);
                return null;
            }

            return trace;
        }
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }

    private boolean samplingEnable(HttpServerRequestImpl request) {
        // optional value
        final String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag={}", samplingFlag);
        }
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private TraceId populateTraceIdFromRequest(HttpServerRequestImpl request) {
        final String transactionId = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {
            final long parentSpanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            final long spanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            final short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);
            final TraceId id = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }


    private void recordRootSpan(final SpanRecorder recorder, final HttpServerRequestImpl request) {
        // root
        recorder.recordServiceType(VertxConstants.VERTX_HTTP_SERVER);
        final String requestURL = request.path();
        if (requestURL != null) {
            recorder.recordRpcName(requestURL);
        }

        if (request.localAddress() != null) {
            final int port = request.localAddress().port();
            if (port <= 0) {
                recorder.recordEndPoint(request.host());
            } else {
                recorder.recordEndPoint(request.host() + ":" + port);
            }
        }

        final String remoteAddr = remoteAddressResolver.resolve(request);
        recorder.recordRemoteAddress(remoteAddr);

        if (!recorder.isRoot()) {
            recordParentInfo(recorder, request);
        }
        recorder.recordApi(VERTX_HTTP_SERVER_METHOD_DESCRIPTOR);

        // record proxy HTTP header.
        this.proxyHttpHeaderRecorder.record(recorder, new ProxyHttpHeaderHandler() {
            @Override
            public String read(String name) {
                return request.getHeader(name);
            }

            @Override
            public void remove(String name) {
                request.headers().remove(name);
            }
        });
    }

    private void recordParentInfo(SpanRecorder recorder, HttpServerRequestImpl request) {
        String parentApplicationName = request.getHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString());
        if (parentApplicationName != null) {
            final String host = request.getHeader(Header.HTTP_HOST.toString());
            if (host != null) {
                recorder.recordAcceptorHost(host);
            } else {
                recorder.recordAcceptorHost(NetworkUtils.getHostFromURL(request.uri().toString()));
            }
            final String type = request.getHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
            final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
            recorder.recordParentApplication(parentApplicationName, parentApplicationType);
        }
    }

    private String getRequestParameter(HttpServerRequestImpl request, int eachLimit, int totalLimit) {
        if (request.params() == null) {
            return "";
        }

        final StringBuilder params = new StringBuilder(64);
        for (Map.Entry<String, String> entry : request.params().entries()) {
            if (params.length() != 0) {
                params.append('&');
            }
            // skip appending parameters if parameter size is bigger than totalLimit
            if (params.length() > totalLimit) {
                params.append("...");
                return params.toString();
            }

            String key = entry.getKey();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append('=');
            Object value = entry.getValue();
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }
        return params.toString();
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

    public static class Bypass<T extends HttpServerRequestImpl> implements RemoteAddressResolver<T> {

        @Override
        public String resolve(T servletRequest) {
            if (servletRequest.remoteAddress() != null) {
                return servletRequest.remoteAddress().toString();
            }
            return "unknown";
        }
    }

    public static class RealIpHeaderResolver<T extends HttpServerRequestImpl> implements RemoteAddressResolver<T> {

        public static final String X_FORWARDED_FOR = "x-forwarded-for";
        public static final String X_REAL_IP = "x-real-ip";
        public static final String UNKNOWN = "unknown";

        private final String realIpHeaderName;
        private final String emptyHeaderValue;

        public RealIpHeaderResolver() {
            this(X_FORWARDED_FOR, UNKNOWN);
        }

        public RealIpHeaderResolver(String realIpHeaderName, String emptyHeaderValue) {
            if (realIpHeaderName == null) {
                throw new NullPointerException("realIpHeaderName must not be null");
            }
            this.realIpHeaderName = realIpHeaderName;
            this.emptyHeaderValue = emptyHeaderValue;
        }

        @Override
        public String resolve(T httpServletRequest) {
            final String realIp = httpServletRequest.getHeader(this.realIpHeaderName);
            if (StringUtils.isEmpty(realIp)) {
                if (httpServletRequest.remoteAddress() != null) {
                    return httpServletRequest.remoteAddress().toString();
                }
                return "";
            }

            if (emptyHeaderValue != null && emptyHeaderValue.equalsIgnoreCase(realIp)) {
                if (httpServletRequest.remoteAddress() != null) {
                    return httpServletRequest.remoteAddress().toString();
                }
                return "";
            }

            final int firstIndex = realIp.indexOf(',');
            if (firstIndex == -1) {
                return realIp;
            } else {
                return realIp.substring(0, firstIndex);
            }
        }
    }
}