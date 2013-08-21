package com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import com.nhn.pinpoint.profiler.context.Header;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.context.TraceID;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.Logger;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.sampler.util.SamplingFlagUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

import com.nhn.pinpoint.common.ServiceType;

/**
 * Method interceptor
 * <p/>
 * <pre>
 * org.apache.http.impl.client.AbstractHttpClient.
 * public <T> T execute(
 *            final HttpHost target,
 *            final HttpRequest request,
 *            final ResponseHandler<? extends T> responseHandler,
 *            final HttpContext context)
 *            throws IOException, ClientProtocolException {
 * </pre>
 */
public class ExecuteMethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(ExecuteMethodInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;
    private TraceContext traceContext;
    //    private int apiId;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final HttpRequest request = (HttpRequest) args[1];
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            if(isDebug) {
                logger.debug("set Sampling flag=false");
            }
            request.addHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();

        TraceID nextId = trace.getTraceId().getNextTraceId();
        trace.recordNextSpanId(nextId.getSpanId());

        final HttpHost host = (HttpHost) args[0];

        // UUID format을 그대로.
        request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
        request.addHeader(Header.HTTP_SPAN_ID.toString(), Integer.toString(nextId.getSpanId()));
        request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Integer.toString(nextId.getParentSpanId()));

        request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
        request.addHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationId());
        request.addHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), String.valueOf(ServiceType.TOMCAT.getCode()));

        trace.recordServiceType(ServiceType.HTTP_CLIENT);

        int port = host.getPort();
        String endpoint = getEndpoint(host.getHostName(), port);
//      DestinationId와 동일하므로 없는게 맞음.
//        trace.recordEndPoint(endpoint);
        trace.recordDestinationId(endpoint);

        trace.recordAttribute(AnnotationKey.HTTP_URL, request.getRequestLine().getUri());

    }

    private String getEndpoint(String host, int port) {
        if (port < 0) {
            return host;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            // result는 로깅하지 않는다.
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            trace.recordApi(descriptor);
            trace.recordException(result);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}