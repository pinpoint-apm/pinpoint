package com.profiler.modifier.connector.interceptors;

import com.profiler.context.*;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.util.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

import com.profiler.interceptor.StaticAroundInterceptor;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private MethodDescriptor descriptor;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

        TraceID nextId = trace.getCurrentTraceId();
        final HttpHost host = (HttpHost) args[0];
        final HttpRequest request = (HttpRequest) args[1];
        // UUID format을 그대로.
        request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
        request.addHeader(Header.HTTP_SPAN_ID.toString(), Long.toString(nextId.getSpanId()));
        request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Long.toString(nextId.getParentSpanId()));
        request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
        request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));


        trace.recordRpcName(request.getProtocolVersion().toString(), "CLIENT");
        trace.recordEndPoint(request.getProtocolVersion().toString() + ":" + host.getHostName() + ":" + host.getPort());
        trace.recordAttribute("http.url", request.getRequestLine().getUri());


    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }

        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.recordApi(descriptor, args);
        trace.recordException(result);

        trace.markAfterTime();
        trace.traceBlockEnd();
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}