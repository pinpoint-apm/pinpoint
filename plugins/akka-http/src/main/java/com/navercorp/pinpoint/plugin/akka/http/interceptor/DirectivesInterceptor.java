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

package com.navercorp.pinpoint.plugin.akka.http.interceptor;

import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpRequest;
import akka.http.scaladsl.server.RequestContextImpl;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.akka.http.AkkaHttpConfig;
import com.navercorp.pinpoint.plugin.akka.http.AkkaHttpConstants;
import com.navercorp.pinpoint.plugin.akka.http.HttpRequestAdaptor;

public class DirectivesInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(DirectivesInterceptor.class);
    private final boolean isTrace = logger.isTraceEnabled();
    private final boolean isDebug = logger.isDebugEnabled();


    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private static final AkkaHttpServerMethodDescriptor AKKA_HTTP_SERVER_METHOD_DESCRIPTOR = new AkkaHttpServerMethodDescriptor();

    private final ProxyRequestRecorder<HttpRequest> proxyRequestRecorder;

    private final RequestAdaptor<HttpRequest> requestAdaptor;
    private final Filter<String> excludeHttpMethodFilter;
    private final Filter<String> excludeUrlFilter;


    public DirectivesInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor, final RequestRecorderFactory<HttpRequest> requestRecorderFactory) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;

        final AkkaHttpConfig config = new AkkaHttpConfig(traceContext.getProfilerConfig());
        this.excludeUrlFilter = config.getExcludeUrlFilter();
        this.excludeHttpMethodFilter = config.getExcludeHttpMethodFilter();
        this.requestAdaptor = new HttpRequestAdaptor(config);
        this.proxyRequestRecorder = requestRecorderFactory.getProxyRequestRecorder(traceContext.getProfilerConfig().isProxyHttpHeaderEnable(), requestAdaptor);
        traceContext.cacheApi(AKKA_HTTP_SERVER_METHOD_DESCRIPTOR);
    }


    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        int requestContextIndex = getArgsIndexOfRequestContext(args);
        if (requestContextIndex == -1) {
            return;
        }

        if (traceContext.currentTraceObject() != null) {
            return;
        }

        RequestContextImpl requestContext = (RequestContextImpl) args[requestContextIndex];
        if (!(requestContext instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid requestContext. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
            }
            return;
        }

        akka.http.scaladsl.model.HttpRequest request = requestContext.request();
        final Trace trace = createTrace(request);
        if (trace == null || !trace.canSampled()) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL);

        final AsyncContext asyncContext = recorder.recordNextAsyncContext(true);
        ((AsyncContextAccessor) requestContext)._$PINPOINT$_setAsyncContext(asyncContext);
        if (isDebug) {
            logger.debug("Set closeable-AsyncContext {}", asyncContext);
        }
    }

    private int getArgsIndexOfRequestContext(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof RequestContextImpl) return i;
        }
        return -1;
    }

    private Trace createTrace(final HttpRequest request) {
        if (request == null) {
            return null;
        }

        final String requestUri = String.valueOf(request.getUri());
        if (requestUri != null && excludeUrlFilter.filter(requestUri)) {
            // skip request.
            if (isTrace) {
                logger.trace("filter requestURI:{}", requestUri);
            }
            return null;
        }

        HttpMethod method = request.method();
        if (method != null && excludeHttpMethodFilter.filter(method.value())) {
            // skip request.
            if (isTrace) {
                logger.trace("filter http method:{}", method.value());
            }
            return null;
        }

        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("Remote call sampling flag found. skip trace requestUrl:{}", requestUri);
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
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}", traceId, requestUri);
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}", traceId, requestUri);
                }
            }
            return trace;
        } else {
            final Trace trace = traceContext.newAsyncTraceObject();
            if (trace.canSampled()) {
                final SpanRecorder recorder = trace.getSpanRecorder();
                recordRootSpan(recorder, request);
            } else {
                if (isDebug) {
                    logger.debug("Sampling is disabled");
                }
            }
            return trace;
        }
    }

    private boolean samplingEnable(HttpRequest request) {
        // optional value
        final String samplingFlag = requestAdaptor.getHeader(request, Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag={}", samplingFlag);
        }
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private TraceId populateTraceIdFromRequest(final HttpRequest request) {
        final String transactionId = requestAdaptor.getHeader(request, Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {
            final long parentSpanID = NumberUtils.parseLong(requestAdaptor.getHeader(request, Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            final long spanID = NumberUtils.parseLong(requestAdaptor.getHeader(request, Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            final short flags = NumberUtils.parseShort(requestAdaptor.getHeader(request, Header.HTTP_FLAGS.toString()), (short) 0);
            final TraceId id = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    private void recordRootSpan(final SpanRecorder recorder, final HttpRequest request) {
        recorder.recordServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER);
        final String requestURL = requestAdaptor.getRpcName(request);
        if (StringUtils.hasLength(requestURL)) {
            recorder.recordRpcName(requestURL);
        }

        String remoteAddress = requestAdaptor.getRemoteAddress(request);
        if (StringUtils.hasLength(remoteAddress)) {
            recorder.recordRemoteAddress(remoteAddress);
        }

        this.proxyRequestRecorder.record(recorder, request);

        if (!recorder.isRoot()) {
            recordParentInfo(recorder, request);
        }
        recorder.recordApi(AKKA_HTTP_SERVER_METHOD_DESCRIPTOR);
    }

    private void recordParentInfo(SpanRecorder recorder, final HttpRequest request) {
        String parentApplicationName = requestAdaptor.getHeader(request, Header.HTTP_PARENT_APPLICATION_NAME.toString());
        if (parentApplicationName != null) {
            final String host = requestAdaptor.getAcceptorHost(request);
            if (host != null) {
                recorder.recordAcceptorHost(host);
            } else {
                String requestURL = String.valueOf(request.getUri());
                recorder.recordAcceptorHost(NetworkUtils.getHostFromURL(requestURL));
            }
            final String type = requestAdaptor.getHeader(request, Header.HTTP_PARENT_APPLICATION_TYPE.toString());
            final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
            recorder.recordParentApplication(parentApplicationName, parentApplicationType);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (getArgsIndexOfRequestContext(args) == -1) {
            return;
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
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
}
