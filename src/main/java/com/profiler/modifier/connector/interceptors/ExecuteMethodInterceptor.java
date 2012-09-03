package com.profiler.modifier.connector.interceptors;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Header;
import com.profiler.context.Trace;
import com.profiler.context.TraceID;
import com.profiler.interceptor.StaticAroundInterceptor;

/**
 * Method interceptor
 * 
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
public class ExecuteMethodInterceptor implements StaticAroundInterceptor {

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		System.out.println("\n\n\n\nHTTP BEFORE");

		HttpHost host = (HttpHost) args[0];
		HttpRequest request = (HttpRequest) args[1];

		TraceID nextId = Trace.getNextId();

		// UUID format을 그대로.
		request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTraceId().toString());
		request.addHeader(Header.HTTP_SPAN_ID.toString(), Long.toString(nextId.getSpanId()));
		request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Long.toString(nextId.getParentSpanId()));
		request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
		request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));

		Trace.recordRpcName("http-call", "");
		Trace.recordEndPoint(host.getHostName(), host.getPort());
		Trace.recordMessage("http.uri=" + request.toString());
		Trace.record(Annotation.ClientSend);

		StopWatch.start("ExecuteMethodInterceptor");
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		System.out.println("\n\n\n\nHTTP AFTER");
		Trace.record(Annotation.ClientRecv, StopWatch.stopAndGetElapsed("ExecuteMethodInterceptor"));
	}
}