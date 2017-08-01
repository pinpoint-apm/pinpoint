package com.navercorp.pinpoint.plugin.resin.interceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
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
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.resin.AsyncAccessor;
import com.navercorp.pinpoint.plugin.resin.HttpServletRequestGetter;
import com.navercorp.pinpoint.plugin.resin.ResinConfig;
import com.navercorp.pinpoint.plugin.resin.ResinConstants;
import com.navercorp.pinpoint.plugin.resin.ServletAsyncMethodDescriptor;
import com.navercorp.pinpoint.plugin.resin.ServletSyncMethodDescriptor;
import com.navercorp.pinpoint.plugin.resin.TraceAccessor;
import com.navercorp.pinpoint.plugin.resin.VersionAccessor;

/**
 * @author huangpengjie@fang.com
 */
public class ServletInvocationInterceptor implements AroundInterceptor {

    public static final String SERVLET_INVOCATION_CLASS_NAME = "com.caucho.server.dispatch.ServletInvocation";

    public static final ServletSyncMethodDescriptor SERVLET_SYNCHRONOUS_API_TAG = new ServletSyncMethodDescriptor();
    public static final ServletAsyncMethodDescriptor SERVLET_ASYNCHRONOUS_API_TAG = new ServletAsyncMethodDescriptor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final boolean isTraceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> excludeProfileMethodFilter;
    private final RemoteAddressResolver<HttpServletRequest> remoteAddressResolver;

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;

    private final boolean isTraceCookies;

    private final SimpleSampler cookieSampler;

    private final DumpType cookieDumpType;
    private final ProxyHttpHeaderRecorder proxyHttpHeaderRecorder;

    public ServletInvocationInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;

        ResinConfig resinConfig = new ResinConfig(traceContext.getProfilerConfig());
        this.excludeUrlFilter = resinConfig.getResinExcludeUrlFilter();
        final String proxyIpHeader = resinConfig.getResinRealIpHeader();
        if (StringUtils.isEmpty(proxyIpHeader)) {
            this.remoteAddressResolver = new Bypass<HttpServletRequest>();
        } else {
            final String tomcatRealIpEmptyValue = resinConfig.getResinRealIpEmptyValue();
            this.remoteAddressResolver = new RealIpHeaderResolver<HttpServletRequest>(proxyIpHeader, tomcatRealIpEmptyValue);
        }
        this.isTraceRequestParam = resinConfig.isResinTraceRequestParam();
        this.excludeProfileMethodFilter = resinConfig.getResinExcludeProfileMethodFilter();
        this.isTraceCookies = resinConfig.isTraceCookies();
        this.cookieSampler = SimpleSamplerFactory.createSampler(isTraceCookies, resinConfig.getCookieSamplingRate());
        this.cookieDumpType = resinConfig.getCookieDumpType();
        this.proxyHttpHeaderRecorder = new ProxyHttpHeaderRecorder(traceContext);

        traceContext.cacheApi(SERVLET_ASYNCHRONOUS_API_TAG);
        traceContext.cacheApi(SERVLET_SYNCHRONOUS_API_TAG);
    }

    @Override
    public void before(Object target, Object[] args) {
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
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(ResinConstants.RESIN_METHOD);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
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
        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        // ------------------------------------------------------
        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            if (this.isTraceRequestParam) {
                final HttpServletRequest request = (HttpServletRequest) args[0];
                if (!excludeProfileMethodFilter.filter(request.getMethod())) {
                    final String parameters = getRequestParameter(request, 64, 512);
                    if (StringUtils.hasLength(parameters)) {
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                    }
                }
                recordRequest(trace, request, throwable);
            }
            final HttpServletResponse response = (HttpServletResponse) args[1];
            if (throwable != null) {
                recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, 500);
                recorder.recordException(throwable);
            } else {
                if (isResinVersion4(target)) {
                    recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, response.getStatus());
                }
            }
            recorder.recordApi(methodDescriptor);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            traceContext.removeTraceObject();
            deleteTrace(trace, target, args, result, throwable);
        }

    }

    private String getRequestParameter(HttpServletRequest request, int eachLimit, int totalLimit) {
        Enumeration<?> attrs = request.getParameterNames();
        final StringBuilder params = new StringBuilder(64);

        while (attrs.hasMoreElements()) {
            if (params.length() != 0) {
                params.append('&');
            }
            // skip appending parameters if parameter size is bigger than  totalLimit
            if (params.length() > totalLimit) {
                params.append("...");
                return params.toString();
            }
            String key = attrs.nextElement().toString();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append('=');
            Object value = request.getParameter(key);
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }
        return params.toString();
    }

    private void deleteTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.traceBlockEnd();

        final HttpServletRequest request = (HttpServletRequest) args[0];
        if (!isAsynchronousProcess(request)) {
            trace.close();
            // reset
            setTraceMetadata(request, null);
        }
    }

    private Trace createTrace(Object target, Object[] args) {
        final HttpServletRequest request = (HttpServletRequest) args[0];

        if (isAsynchronousProcess(request)) {
            // servlet 3.0
            final Trace trace = getTraceMetadata(request);
            if (trace != null) {
                // change api
                SpanRecorder recorder = trace.getSpanRecorder();
                recorder.recordApi(SERVLET_ASYNCHRONOUS_API_TAG);
                // attach current thread local.
                traceContext.continueTraceObject(trace);
                return trace;
            }
        }

        final Trace currentRawTraceObject = traceContext.currentRawTraceObject();
        if (currentRawTraceObject != null) {
            return currentRawTraceObject;
        }
        final String requestURI = request.getRequestURI();
        if (excludeUrlFilter.filter(requestURI)) {
            if (isDebug) {
                logger.debug("filter requestURI:{}", requestURI);
            }
            return null;
        }

        // check sampling flag from client. If the flag is false, do not sample this request.
        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            // Even if this transaction is not a sampling target, we have to create Trace object to mark 'not sampling'.
            // For example, if this transaction invokes rpc call, we can add parameter to tell remote node 'don't sample this transaction'
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
            }
            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            // TODO Maybe we should decide to trace or not even if the sampling  flag is true to prevent too many requests are traced.
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
                setTraceMetadata(request, trace);
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, request.getRequestURI(), request.getRemoteAddr());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, request.getRequestURI(), request.getRemoteAddr());
                }
            }
            return trace;
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
                setTraceMetadata(request, trace);
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{} , traceId:{}", request.getRequestURI(), request.getRemoteAddr(), trace.getTraceId());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
                }
            }
            return trace;
        }
    }

    private void setTraceMetadata(final HttpServletRequest request, final Trace trace) {
        if (request instanceof TraceAccessor) {
            ((TraceAccessor) request)._$PINPOINT$_setTrace(trace);
        }
    }

    private Trace getTraceMetadata(HttpServletRequest request) {
        if (request instanceof HttpServletRequestGetter) {
            request = ((HttpServletRequestGetter) request)._$PINPOINT$_getRequest();
        }
        if (!(request instanceof TraceAccessor)) {
            return null;
        }
        return ((TraceAccessor) request)._$PINPOINT$_getTrace();
    }

    private boolean getAsyncMetadata(HttpServletRequest request) {
        // AsyncRequest
        if (request instanceof HttpServletRequestGetter) {
            request = ((HttpServletRequestGetter) request)._$PINPOINT$_getRequest();
        }
        if (!(request instanceof AsyncAccessor)) {
            return false;
        }

        return ((AsyncAccessor) request)._$PINPOINT$_isAsync();
    }

    private boolean isAsynchronousProcess(HttpServletRequest request) {
        if (getTraceMetadata(request) == null) {
            return false;
        }
        return getAsyncMetadata(request);
    }

    private void recordRootSpan(final SpanRecorder recorder, final HttpServletRequest request) {
        // root
        recorder.recordServiceType(ResinConstants.RESIN);

        final String requestURL = request.getRequestURI();
        recorder.recordRpcName(requestURL);

        final int port = request.getServerPort();
        final String endPoint = HostAndPort.toHostAndPortString(request.getServerName(), port);
        recorder.recordEndPoint(endPoint);

        final String remoteAddr = remoteAddressResolver.resolve(request);
        recorder.recordRemoteAddress(remoteAddr);

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

    private void recordParentInfo(SpanRecorder recorder, HttpServletRequest request) {
        String parentApplicationName = request.getHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString());
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

    /**
     * Populate source trace from HTTP Header.
     *
     * @param request
     * @return TraceId when it is possible to get a transactionId from Http header. if not possible return null
     */
    private TraceId populateTraceIdFromRequest(HttpServletRequest request) {

        String transactionId = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {

            long parentSpanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            long spanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            final TraceId id = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    private boolean samplingEnable(HttpServletRequest request) {
        // optional value
        final String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag:{}", samplingFlag);
        }
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private static class Bypass<T extends HttpServletRequest> implements RemoteAddressResolver<T> {

        @Override
        public String resolve(T servletRequest) {
            return servletRequest.getRemoteAddr();
        }
    }

    private class RealIpHeaderResolver<T extends HttpServletRequest> implements RemoteAddressResolver<T> {

        public static final String X_FORWARDED_FOR = "x-forwarded-for";
        @SuppressWarnings("unused")
        public static final String X_REAL_IP = "x-real-ip";
        public static final String UNKNOWN = "unknown";

        private final String realIpHeaderName;
        private final String emptyHeaderValue;

        @SuppressWarnings("unused")
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
                return httpServletRequest.getRemoteAddr();
            }

            if (emptyHeaderValue != null && emptyHeaderValue.equalsIgnoreCase(realIp)) {
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

    public boolean isResinVersion4(Object target) {
        if (!(target instanceof VersionAccessor)) {
            logger.debug(" resin version 3 ");
            return false;
        } else {
            logger.debug(" resin version 4 ");
            return true;
        }

    }

    private void recordRequest(Trace trace, HttpServletRequest request, Throwable throwable) {
        final boolean isException = InterceptorUtils.isThrowable(throwable);
        if (isTraceCookies) {
            if (DumpType.ALWAYS == cookieDumpType) {
                recordCookie(request, trace);
            } else if (DumpType.EXCEPTION == cookieDumpType && isException) {
                recordCookie(request, trace);
            }
        }
    }

    private void recordCookie(HttpServletRequest request, Trace trace) {
        if (cookieSampler.isSampling()) {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            Map<String, Object> cookies = ReadCookieMap(request);
            recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, cookies);
        }
        return;
    }

    public Map<String, Object> ReadCookieMap(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            Cookie[] cookies = request.getCookies();
            if (null != cookies && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    params.put(cookie.getName(), cookie.getValue());
                }
            }
        } catch (NullPointerException e) {
            logger.info(" resin without ROOT project ");
        }
        return params;
    }
}
