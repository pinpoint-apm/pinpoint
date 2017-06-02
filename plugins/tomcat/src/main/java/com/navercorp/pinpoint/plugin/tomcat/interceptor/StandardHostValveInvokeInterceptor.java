/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.navercorp.pinpoint.bootstrap.context.*;
import java.util.List;
import java.util.Arrays;
 
import org.apache.catalina.connector.Response;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.tomcat.AsyncAccessor;
import com.navercorp.pinpoint.plugin.tomcat.ServletAsyncMethodDescriptor;
import com.navercorp.pinpoint.plugin.tomcat.ServletSyncMethodDescriptor;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConfig;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;
import com.navercorp.pinpoint.plugin.tomcat.TraceAccessor;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class StandardHostValveInvokeInterceptor implements AroundInterceptor {
    public static final ServletSyncMethodDescriptor SERVLET_SYNCHRONOUS_API_TAG = new ServletSyncMethodDescriptor();
    public static final ServletAsyncMethodDescriptor SERVLET_ASYNCHRONOUS_API_TAG = new ServletAsyncMethodDescriptor();

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final boolean isTraceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> excludeProfileMethodFilter;
    private final RemoteAddressResolver<HttpServletRequest> remoteAddressResolver;
    private String successCodes;
    private boolean enablefailurecodes;
    private MethodDescriptor methodDescriptor;
    private TraceContext traceContext;

    public StandardHostValveInvokeInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;

        TomcatConfig tomcatConfig = new TomcatConfig(traceContext.getProfilerConfig());
        this.excludeUrlFilter = tomcatConfig.getTomcatExcludeUrlFilter();

        final String proxyIpHeader = tomcatConfig.getTomcatRealIpHeader();
        if (StringUtils.isEmpty(proxyIpHeader)) {
            this.remoteAddressResolver = new Bypass<HttpServletRequest>();
        } else {
            final String tomcatRealIpEmptyValue = tomcatConfig.getTomcatRealIpEmptyValue();
            this.remoteAddressResolver = new RealIpHeaderResolver<HttpServletRequest>(proxyIpHeader, tomcatRealIpEmptyValue);
        }
        this.isTraceRequestParam = tomcatConfig.isTomcatTraceRequestParam();
        this.excludeProfileMethodFilter = tomcatConfig.getTomcatExcludeProfileMethodFilter();
        this.successCodes = tomcatConfig.getSuccessCodes();
        this.enablefailurecodes =  tomcatConfig.isTomcatEnablefailureCodes();
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
            recorder.recordServiceType(TomcatConstants.TOMCAT_METHOD);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    public static class Bypass<T extends HttpServletRequest> implements RemoteAddressResolver<T> {

        @Override
        public String resolve(T servletRequest) {
            return servletRequest.getRemoteAddr();
        }
    }

    public static class RealIpHeaderResolver<T extends HttpServletRequest> implements RemoteAddressResolver<T> {

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
            // For example, if this transaction invokes rpc call, we can add parameter to tell remote node 'don't sample this transaction'
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
            }
            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            // TODO Maybe we should decide to trace or not even if the sampling flag is true to prevent too many requests are traced.
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

    private void setTraceMetadata(final HttpServletRequest request, final Trace trace) {
        if (request instanceof TraceAccessor) {
            ((TraceAccessor) request)._$PINPOINT$_setTrace(trace);
        }
    }

    private Trace getTraceMetadata(final HttpServletRequest request) {
        if (!(request instanceof TraceAccessor)) {
            return null;
        }

        return ((TraceAccessor) request)._$PINPOINT$_getTrace();
    }

    private boolean getAsyncMetadata(final HttpServletRequest request) {
        if (!(request instanceof AsyncAccessor)) {
            return false;
        }

        return ((AsyncAccessor) request)._$PINPOINT$_isAsync();
    }

    private boolean isAsynchronousProcess(final HttpServletRequest request) {
        if (getTraceMetadata(request) == null) {
            return false;
        }

        return getAsyncMetadata(request);
    }

    private void recordRootSpan(final SpanRecorder recorder, final HttpServletRequest request) {
        // root
        recorder.recordServiceType(TomcatConstants.TOMCAT);

        final String requestURL = request.getRequestURI();
        recorder.recordRpcName(requestURL);

        final int port = request.getServerPort();
        final String endPoint = request.getServerName() + ":" + port;
        recorder.recordEndPoint(endPoint);

        final String remoteAddr = remoteAddressResolver.resolve(request);
        recorder.recordRemoteAddress(remoteAddr);

        if (!recorder.isRoot()) {
            recordParentInfo(recorder, request);
        }
        recorder.recordApi(SERVLET_SYNCHRONOUS_API_TAG);
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
                    if (StringUtils.isNotEmpty(parameters)) {
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                    }
                }
            }

            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);

			List<String> successports = Arrays.asList(successCodes.split(","));

			Response response = (Response) args[1];
			if (!successports.contains(Integer.toString(response.getStatus())) && this.enablefailurecodes) {
				String errorMessage = "HTTP Response status code is " + response.getStatus() + " - "
						+ getReasonMessage(response.getStatus());
				recorder.recordError(true, errorMessage);
			}
        } catch (Throwable th) {
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

    private String getRequestParameter(HttpServletRequest request, int eachLimit, int totalLimit) {
        Enumeration<?> attrs = request.getParameterNames();
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
            String key = attrs.nextElement().toString();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append("=");
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
    
	private String getReasonMessage(int status) {
		String reason;
		switch (status) {
		case 200:
			reason = "OK";
			break;
		case 201:
			reason = "Created";
			break;
		case 202:
			reason = "Accepted";
			break;
		case 203:
			reason = "Non-Authoritative Information";
			break;
		case 204:
			reason = "No Content";
			break;
		case 205:
			reason = "Reset Content";
			break;
		case 206:
			reason = "Partial Content";
			break;
		case 207:
			reason = "Multi-Status";
			break;
		case 208:
			reason = "Already Reported";
			break;
		case 226:
			reason = "IM Used";
			break;
		case 300:
			reason = "Multiple Choices";
			break;
		case 303:
			reason = "See Other";
			break;
		case 301:
			reason = "Moved Permanently";
			break;
		case 302:
			reason = "Found";
			break;
		case 305:
			reason = "Use Proxy";
			break;
		case 307:
			reason = "Temporary Redirect";
			break;
		case 308:
			reason = "Permanent Redirect";
			break;
		case 400:
			reason = "Bad Request";
			break;
		case 401:
			reason = "Unauthorized";
			break;
		case 402:
			reason = "Payment Required";
			break;
		case 403:
			reason = "Forbidden";
			break;
		case 404:
			reason = "Not Found";
			break;
		case 405:
			reason = "Method Not Allowed";
			break;
		case 406:
			reason = "Not Acceptable";
			break;
		case 407:
			reason = "Proxy Authentication Required";
			break;
		case 408:
			reason = "Request Timeout";
			break;
		case 409:
			reason = "Conflict";
			break;
		case 410:
			reason = "Gone";
			break;
		case 411:
			reason = "Length Required";
			break;
		case 412:
			reason = "Preconditioned Failed";
			break;
		case 413:
			reason = "Request Entity Too Large";
			break;
		case 414:
			reason = "Request-URI Too Long";
			break;
		case 415:
			reason = "Unsupported Media Type";
			break;
		case 416:
			reason = "Request Range Not Satifiable";
			break;
		case 417:
			reason = "Expectation Failed";
			break;
		case 422:
			reason = "Unprocessable Entity";
			break;
		case 423:
			reason = "Locked";
			break;
		case 424:
			reason = "Failed Dependency";
			break;
		case 425:
			reason = "Reserved for WebDAV";
			break;
		case 426:
			reason = "Upgrade Required";
			break;
		case 428:
			reason = "Precondition Required";
			break;
		case 429:
			reason = "Too Many Requests";
			break;
		case 431:
			reason = "Request Header Fields Too Large";
			break;
		case 451:
			reason = "Unavailable For Legal Reasons";
			break;
		case 500:
			reason = "Internal Server Error";
			break;
		case 501:
			reason = "Not Implemented";
			break;
		case 502:
			reason = "Bad Gateway";
			break;
		case 503:
			reason = "Service Unavailable";
			break;
		case 504:
			reason = "Gateway Timeout";
			break;
		case 505:
			reason = "HTTP Version Not Supported";
			break;
		case 506:
			reason = "Variant Also Negotiates";
			break;
		case 507:
			reason = "Insufficient Storage";
			break;
		case 510:
			reason = "Not Extended";
			break;
		case 511:
			reason = "Network Authentication Required";
			break;
		default:
			reason = "Unknown";
			break;
		}
		return reason;
	}
}
