package com.profiler.modifier.connector.httpclient4.interceptor;

import java.util.logging.Logger;

import com.profiler.common.AnnotationKey;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

import com.profiler.common.ServiceType;
import com.profiler.context.Header;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.context.TraceID;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggingUtils;

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
public class ExecuteMethodInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

    private final Logger logger = Logger.getLogger(ExecuteMethodInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private MethodDescriptor descriptor;
//    private int apiId;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
        }
        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

        TraceID nextId = trace.getTraceId().getNextTraceId();
        trace.recordNextSpanId(nextId.getSpanId());
        
        final HttpHost host = (HttpHost) args[0];
        final HttpRequest request = (HttpRequest) args[1];
        // UUID format을 그대로.
        request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
        request.addHeader(Header.HTTP_SPAN_ID.toString(), Integer.toString(nextId.getSpanId()));
        request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Integer.toString(nextId.getParentSpanId()));
        request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
        request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
		request.addHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationId());
		request.addHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), String.valueOf(ServiceType.TOMCAT.getCode()));

        trace.recordServiceType(ServiceType.HTTP_CLIENT);

		int port = host.getPort();
//		trace.recordEndPoint(host.getHostName() +  ((port > 0) ? ":" + port : ""));
        trace.recordDestinationId(host.getHostName() +  ((port > 0) ? ":" + port : ""));

		trace.recordAttribute(AnnotationKey.HTTP_URL, request.getRequestLine().getUri());
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            // result는 로깅하지 않는다.
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args);
        }

        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
		trace.recordApi(descriptor);
//        trace.recordApi(this.apiId);
        trace.recordException(result);

        trace.markAfterTime();
        trace.traceBlockEnd();
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        TraceContext traceContext = TraceContext.getTraceContext();
        traceContext.cacheApi(descriptor);
    }

}