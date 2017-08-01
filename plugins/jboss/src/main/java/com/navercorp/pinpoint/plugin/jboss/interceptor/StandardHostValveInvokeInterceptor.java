/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jboss.interceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.RemoteAddressResolver;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderHandler;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.jboss.AsyncAccessor;
import com.navercorp.pinpoint.plugin.jboss.JbossConfig;
import com.navercorp.pinpoint.plugin.jboss.JbossConstants;
import com.navercorp.pinpoint.plugin.jboss.ServletAsyncMethodDescriptor;
import com.navercorp.pinpoint.plugin.jboss.ServletSyncMethodDescriptor;
import com.navercorp.pinpoint.plugin.jboss.TraceAccessor;

/**
 * The Class StandardHostValveInvokeInterceptor.
 *
 * @author emeroad
 * @author jaehong.kim
 */
public class StandardHostValveInvokeInterceptor implements AroundInterceptor {

    /** The Constant SERVLET_SYNCHRONOUS_API_TAG. */
    public static final ServletSyncMethodDescriptor SERVLET_SYNCHRONOUS_API_TAG = new ServletSyncMethodDescriptor();

    /** The Constant SERVLET_ASYNCHRONOUS_API_TAG. */
    public static final ServletAsyncMethodDescriptor SERVLET_ASYNCHRONOUS_API_TAG = new ServletAsyncMethodDescriptor();

    /** The logger. */
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    /** The is debug. */
    private final boolean isDebug = logger.isDebugEnabled();

    /** The is trace. */
    private final boolean isTrace = logger.isTraceEnabled();

    /** The is trace request param. */
    private final boolean isTraceRequestParam;

    /** The exclude url filter. */
    private final Filter<String> excludeUrlFilter;

    /** The exclude profile method filter. */
    private final Filter<String> excludeProfileMethodFilter;

    /** The remote address resolver. */
    private final RemoteAddressResolver<HttpServletRequest> remoteAddressResolver;

    /** The method descriptor. */
    private final MethodDescriptor methodDescriptor;

    /** The trace context. */
    private final TraceContext traceContext;

    private final ProxyHttpHeaderRecorder proxyHttpHeaderRecorder;

    /**
     * Instantiates a new standard host valve invoke interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor the descriptor
     */
    public StandardHostValveInvokeInterceptor(final TraceContext traceContext, final MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;

        JbossConfig jbossConfig = new JbossConfig(traceContext.getProfilerConfig());
        this.excludeUrlFilter = jbossConfig.getJbossExcludeUrlFilter();

        final String proxyIpHeader = jbossConfig.getJbossRealIpHeader();
        if (StringUtils.isEmpty(proxyIpHeader)) {
            this.remoteAddressResolver = new Bypass<HttpServletRequest>();
        } else {
            final String jbossRealIpEmptyValue = jbossConfig.getJbossRealIpEmptyValue();
            this.remoteAddressResolver = new RealIpHeaderResolver<HttpServletRequest>(proxyIpHeader, jbossRealIpEmptyValue);
        }
        this.isTraceRequestParam = jbossConfig.isJbossTraceRequestParam();
        this.excludeProfileMethodFilter = jbossConfig.getJbossExcludeProfileMethodFilter();
        this.proxyHttpHeaderRecorder = new ProxyHttpHeaderRecorder(traceContext);

        traceContext.cacheApi(SERVLET_ASYNCHRONOUS_API_TAG);
        traceContext.cacheApi(SERVLET_SYNCHRONOUS_API_TAG);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor#before(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void before(final Object target, final Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final Trace trace = createTrace(target, args);
            if (trace == null) {
                return;
            }
            // TODO STATDISABLE this logic was added to disable statistics tracing
            if (!trace.canSampled()) {
                return;
            }
            // ------------------------------------------------------
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(JbossConstants.JBOSS_METHOD);
        } catch (final Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    /**
     * The Class Bypass.
     *
     * @param <T> the generic type
     */
    public static class Bypass<T extends HttpServletRequest> implements RemoteAddressResolver<T> {

        /*
         * (non-Javadoc)
         * 
         * @see com.navercorp.pinpoint.bootstrap.context.RemoteAddressResolver#resolve(java.lang.Object)
         */
        @Override
        public String resolve(final T servletRequest) {
            return servletRequest.getRemoteAddr();
        }
    }

    /**
     * The Class RealIpHeaderResolver.
     *
     * @param <T> the generic type
     */
    public static class RealIpHeaderResolver<T extends HttpServletRequest> implements RemoteAddressResolver<T> {

        /** The Constant X_FORWARDED_FOR. */
        public static final String X_FORWARDED_FOR = "x-forwarded-for";

        /** The Constant X_REAL_IP. */
        public static final String X_REAL_IP = "x-real-ip";

        /** The Constant UNKNOWN. */
        public static final String UNKNOWN = "unknown";

        /** The real ip header name. */
        private final String realIpHeaderName;

        /** The empty header value. */
        private final String emptyHeaderValue;

        /**
         * Instantiates a new real ip header resolver.
         */
        public RealIpHeaderResolver() {
            this(X_FORWARDED_FOR, UNKNOWN);
        }

        /**
         * Instantiates a new real ip header resolver.
         *
         * @param realIpHeaderName the real ip header name
         * @param emptyHeaderValue the empty header value
         */
        public RealIpHeaderResolver(final String realIpHeaderName, final String emptyHeaderValue) {
            if (realIpHeaderName == null) {
                throw new NullPointerException("realIpHeaderName must not be null");
            }
            this.realIpHeaderName = realIpHeaderName;
            this.emptyHeaderValue = emptyHeaderValue;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.navercorp.pinpoint.bootstrap.context.RemoteAddressResolver#resolve(java.lang.Object)
         */
        @Override
        public String resolve(final T httpServletRequest) {
            final String realIp = httpServletRequest.getHeader(this.realIpHeaderName);

            if (StringUtils.isEmpty(realIp)) {
                return httpServletRequest.getRemoteAddr();
            }

            if ((emptyHeaderValue != null) && emptyHeaderValue.equalsIgnoreCase(realIp)) {
                return httpServletRequest.getRemoteAddr();
            }

            final int firstIndex = realIp.indexOf(',');
            if (firstIndex == -1) {
                return realIp;
            } else {
                return realIp.substring(0, firstIndex);
            }
        }
    }

    /**
     * Creates the trace.
     *
     * @param target the target
     * @param args the args
     * @return the trace
     */
    private Trace createTrace(final Object target, final Object[] args) {
        final HttpServletRequest request = (HttpServletRequest) args[0];
        if (isAsynchronousProcess(request)) {
            // servlet 3.0
            final Trace trace = getTraceMetadata(request);
            if (trace != null) {
                // change api
                final SpanRecorder recorder = trace.getSpanRecorder();
                recorder.recordApi(SERVLET_ASYNCHRONOUS_API_TAG);
                // attach current thread local.
                traceContext.continueTraceObject(trace);

                return trace;
            }
        }

        final String requestURI = request.getRequestURI();
        if (excludeUrlFilter.filter(requestURI)) {
            if (isTrace) {
                logger.trace("filter requestURI:{}", requestURI);
            }
            return null;
        }

        // check sampling flag from client. If the flag is false, do not sample this request.
        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            // Even if this transaction is not a sampling target, we have to create Trace object to mark 'not sampling'.
            // For example, if this transaction invokes rpc call, we can add parameter to tell remote node 'don't sample this
            // transaction'
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
            }
            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            // TODO Maybe we should decide to trace or not even if the sampling flag is true to prevent too many requests are
            // traced.
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                final SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
                setTraceMetadata(request, trace);
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, request.getRequestURI(), request.getRemoteAddr());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, request.getRequestURI(),
                        request.getRemoteAddr());
                }
            }
            return trace;
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                final SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
                setTraceMetadata(request, trace);
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
                }
            }
            return trace;
        }
    }

    /**
     * Sets the trace metadata.
     *
     * @param request the request
     * @param trace the trace
     */
    private void setTraceMetadata(final HttpServletRequest request, final Trace trace) {
        if (request instanceof TraceAccessor) {
            ((TraceAccessor) request)._$PINPOINT$_setTrace(trace);
        }
    }

    /**
     * Gets the trace metadata.
     *
     * @param request the request
     * @return the trace metadata
     */
    private Trace getTraceMetadata(final HttpServletRequest request) {
        if (!(request instanceof TraceAccessor)) {
            return null;
        }

        return ((TraceAccessor) request)._$PINPOINT$_getTrace();
    }

    /**
     * Gets the async metadata.
     *
     * @param request the request
     * @return the async metadata
     */
    private boolean getAsyncMetadata(final HttpServletRequest request) {
        if (!(request instanceof AsyncAccessor)) {
            return false;
        }

        return ((AsyncAccessor) request)._$PINPOINT$_isAsync();
    }

    /**
     * Checks if is asynchronous process.
     *
     * @param request the request
     * @return true, if is asynchronous process
     */
    private boolean isAsynchronousProcess(final HttpServletRequest request) {
        if (getTraceMetadata(request) == null) {
            return false;
        }

        return getAsyncMetadata(request);
    }

    /**
     * Record root span.
     *
     * @param recorder the recorder
     * @param request the request
     */
    private void recordRootSpan(final SpanRecorder recorder, final HttpServletRequest request) {
        // root
        recorder.recordServiceType(JbossConstants.JBOSS);

        final String requestURL = request.getRequestURI();
        recorder.recordRpcName(requestURL);

        final int port = request.getServerPort();
        final String endPoint = HostAndPort.toHostAndPortString(request.getServerName(), port);
        recorder.recordEndPoint(endPoint);

        final String remoteAddress = remoteAddressResolver.resolve(request);
        // TODO API safety check required
        // Potential Risk
        // 1. Implicit DNSLookup (InetAddress.getByName())
        // 2. DNSLookup can cause thread blocking.
        // 3. difficult format parsing problem in collector
//        recorder.recordRemoteAddress(JbossUtility.fetchRemoteAddressDetails(remoteAddress));
        recorder.recordRemoteAddress(remoteAddress);
        if (!recorder.isRoot()) {
            recordParentInfo(recorder, request);
        }
        recorder.recordApi(SERVLET_SYNCHRONOUS_API_TAG);

        // record proxy HTTP headers.
        this.proxyHttpHeaderRecorder.record(recorder, new ProxyHttpHeaderHandler() {
            @Override
            public String read(String name) {
                return request.getHeader(name);
            }

            @Override
            public void remove(String name) {
                // TODO
            }
        });
    }

    /**
     * Record parent info.
     *
     * @param recorder the recorder
     * @param request the request
     */
    private void recordParentInfo(final SpanRecorder recorder, final HttpServletRequest request) {
        final String parentApplicationName = request.getHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString());
        if (parentApplicationName != null) {
            final String host = request.getHeader(Header.HTTP_HOST.toString());
            if (host != null) {
                recorder.recordAcceptorHost(host);
            } else {
                recorder.recordAcceptorHost(NetworkUtils.getHostFromURL(request.getRequestURL().toString()));
            }
            final String type = request.getHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
            final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
            recorder.recordParentApplication(parentApplicationName, parentApplicationType);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor#after(java.lang.Object, java.lang.Object[],
     * java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void after(final Object target, final Object[] args, final Object result, final Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        // ------------------------------------------------------
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            if (this.isTraceRequestParam) {
                final HttpServletRequest request = (HttpServletRequest) args[0];
                if (!excludeProfileMethodFilter.filter(request.getMethod())) {
                    final String parameters = getRequestParameter(request, 64, 512);
                    if (StringUtils.hasLength(parameters)) {
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                    }
                }
            }

            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
        } catch (final Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            traceContext.removeTraceObject();
            deleteTrace(trace, target, args, result, throwable);
        }
    }

    /**
     * Populate source trace from HTTP Header.
     *
     * @param request the request
     * @return TraceId when it is possible to get a transactionId from Http header. if not possible return null
     */
    private TraceId populateTraceIdFromRequest(final HttpServletRequest request) {

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

    /**
     * Sampling enable.
     *
     * @param request the request
     * @return true, if successful
     */
    private boolean samplingEnable(final HttpServletRequest request) {
        // optional value
        final String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag:{}", samplingFlag);
        }
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    /**
     * Gets the request parameter.
     *
     * @param request the request
     * @param eachLimit the each limit
     * @param totalLimit the total limit
     * @return the request parameter
     */
    private String getRequestParameter(final HttpServletRequest request, final int eachLimit, final int totalLimit) {
        final Enumeration<?> attrs = request.getParameterNames();
        final StringBuilder params = new StringBuilder(64);

        while (attrs.hasMoreElements()) {
            if (params.length() != 0) {
                params.append('&');
            }
            // skip appending parameters if parameter size is bigger than totalLimit
            if (params.length() > totalLimit) {
                params.append("...");
                return params.toString();
            }
            final String key = attrs.nextElement().toString();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append('=');
            final Object value = request.getParameter(key);
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }
        return params.toString();
    }

    /**
     * Delete trace.
     *
     * @param trace the trace
     * @param target the target
     * @param args the args
     * @param result the result
     * @param throwable the throwable
     */
    private void deleteTrace(final Trace trace, final Object target, final Object[] args, final Object result, final Throwable throwable) {
        trace.traceBlockEnd();

        final HttpServletRequest request = (HttpServletRequest) args[0];
        if (!isAsynchronousProcess(request)) {
            trace.close();
            // reset
            setTraceMetadata(request, null);
        }
    }
}
