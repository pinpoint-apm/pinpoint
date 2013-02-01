package com.profiler.modifier.connector.interceptors;

import java.util.Arrays;
import java.util.logging.Logger;

import com.profiler.logging.LoggingUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

import com.profiler.common.AnnotationNames;
import com.profiler.common.ServiceType;
import com.profiler.context.Header;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.context.TraceID;
import com.profiler.interceptor.ApiIdSupport;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.StringUtils;

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
        request.addHeader(Header.HTTP_SPAN_ID.toString(), Long.toString(nextId.getSpanId()));
        request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Long.toString(nextId.getParentSpanId()));
        request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
        request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
        
		trace.recordRpcName(ServiceType.HTTP_CLIENT, request.getProtocolVersion().toString(), "CLIENT");
		
		int port = host.getPort();
		trace.recordEndPoint(request.getProtocolVersion() + ":" + host.getHostName() +  ((port > 0) ? ":" + port : ""));
		trace.recordAttribute(AnnotationNames.HTTP_URL, request.getRequestLine().getUri());
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            logger.fine("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
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