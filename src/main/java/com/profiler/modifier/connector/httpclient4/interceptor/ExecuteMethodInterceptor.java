package com.profiler.modifier.connector.httpclient4.interceptor;

import com.profiler.logging.Logger;

import com.profiler.common.AnnotationKey;
import com.profiler.context.*;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.logging.LoggerFactory;
import com.profiler.sampler.util.SamplingFlagUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

import com.profiler.common.ServiceType;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;

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
public class ExecuteMethodInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(ExecuteMethodInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;
    private TraceContext traceContext;
    //    private int apiId;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, className, methodName, parameterDescription, args);
        }
        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final HttpRequest request = (HttpRequest) args[1];
        final boolean sampling = trace.canSampled();
        if (!sampling) {
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
        trace.recordDestinationId(host.getHostName() +  ((port > 0) ? ":" + port : ""));

        trace.recordAttribute(AnnotationKey.HTTP_URL, request.getRequestLine().getUri());

    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            // result는 로깅하지 않는다.
            logger.afterInterceptor(target, className, methodName, parameterDescription, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
		trace.recordApi(descriptor);
        trace.recordException(result);

        trace.markAfterTime();
        trace.traceBlockEnd();
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