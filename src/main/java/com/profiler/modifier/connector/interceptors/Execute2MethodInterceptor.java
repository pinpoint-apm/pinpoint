package com.profiler.modifier.connector.interceptors;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;

import com.profiler.common.ServiceType;
import com.profiler.context.Header;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.context.TraceID;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.StringUtils;

/**
 * Method interceptor
 * <p/>
 * 
 * <pre>
 * org.apache.http.impl.client.AbstractHttpClient.
 * public final HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException
 * </pre>
 */
public class Execute2MethodInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

	private final Logger logger = Logger.getLogger(Execute2MethodInterceptor.class.getName());
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

		final HttpUriRequest request = (HttpUriRequest) args[0];

		// UUID format을 그대로.
		request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
		request.addHeader(Header.HTTP_SPAN_ID.toString(), Long.toString(nextId.getSpanId()));
		request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Long.toString(nextId.getParentSpanId()));
		request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
		request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));

		HttpHost host = URIUtils.extractHost(request.getURI());

		trace.recordRpcName(ServiceType.HTTP_CLIENT, request.getProtocolVersion().toString(), "CLIENT");
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
		trace.recordApi(descriptor);
		trace.recordException(result);

		trace.markAfterTime();
		trace.traceBlockEnd();
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
	}
}