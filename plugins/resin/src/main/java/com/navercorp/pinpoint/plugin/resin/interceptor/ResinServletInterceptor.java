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

package com.navercorp.pinpoint.plugin.resin.interceptor;

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
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.resin.ResinConfig;
import com.navercorp.pinpoint.plugin.resin.ResinConstants;
import com.navercorp.pinpoint.plugin.resin.ServletMethodDescriptor;

/**
 * 
 * @author baiyang
 *
 */
public class ResinServletInterceptor implements AroundInterceptor {

    public static final ServletMethodDescriptor SERVLET_API_TAG = new ServletMethodDescriptor();

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final RemoteAddressResolver<HttpServletRequest> remoteAddressResolver;

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;
    private final Filter<String> excludeUrlFilter;

    public ResinServletInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        ResinConfig resinConfig = new ResinConfig(traceContext.getProfilerConfig());
        this.methodDescriptor = descriptor;
        this.traceContext = traceContext;
        this.remoteAddressResolver = new Bypass<HttpServletRequest>();
        this.excludeUrlFilter = resinConfig.getResinExcludeUrlFilter();
        traceContext.cacheApi(SERVLET_API_TAG);
    }

    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        try {
            final Trace trace = createTrace(target, args);
            if (trace == null) {
                return;
            }
            if (!trace.canSampled()) {
                return;
            }
            trace.traceBlockBegin();
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private Trace createTrace(Object target, Object[] args) {
        final HttpServletRequest request = (HttpServletRequest) args[0];

        final String requestURI = request.getRequestURI();
        if (excludeUrlFilter.filter(requestURI)) {
            if (isTrace) {
                logger.trace("filter requestURI:{}", requestURI);
            }
            return null;
        }

        // check sampling flag from client. If the flag is false, do not sample this request.
        final boolean sampling = this.samplingEnable(request);
        if (!sampling) {
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace requestUrl:{}, remoteAddr:{}",
                        request.getRequestURI(), request.getRemoteAddr());
            }
            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId,
                            request.getRequestURI(), request.getRemoteAddr());
                }
            } else {
                if (isDebug) {
                    logger.debug(
                            "TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}",
                            traceId, request.getRequestURI(), request.getRemoteAddr());
                }
            }
            return trace;
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}",
                            request.getRequestURI(), request.getRemoteAddr());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}",
                            request.getRequestURI(), request.getRemoteAddr());
                }
            }
            return trace;
        }
    }

    private void recordRootSpan(final SpanRecorder recorder, final HttpServletRequest request) {
        // root
        recorder.recordServiceType(ResinConstants.RESIN);

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
        recorder.recordApi(SERVLET_API_TAG);
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

    private boolean samplingEnable(HttpServletRequest request) {
        final String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag:{}", samplingFlag);
        }
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private TraceId populateTraceIdFromRequest(HttpServletRequest request) {

        String transactionId = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {

            long parentSpanID =
                    NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
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

    public static class Bypass<T extends HttpServletRequest> implements RemoteAddressResolver<T> {
        public String resolve(T servletRequest) {
            return servletRequest.getRemoteAddr();
        }
    }

    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            traceContext.removeTraceObject();
            trace.traceBlockEnd();
            trace.close();
        }
    }

}
