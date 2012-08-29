package com.profiler.modifier.connector.interceptors;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Header;
import com.profiler.context.Trace;
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

		request.addHeader(Header.HTTP_TRACE_ID.toString(), Trace.getTraceId().getTraceId());
		request.addHeader(Header.HTTP_SPAN_ID.toString(), Trace.getTraceId().getSpanId());
		request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Trace.getTraceId().getParentSpanId());
		request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(Trace.getTraceId().isSampled()));
		request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(Trace.getTraceId().getFlags()));

		Trace.recordRpcName("http-call", "");
		Trace.recordServerAddr(host.getHostName(), host.getPort());
		Trace.record("http.uri=" + request.toString());
		Trace.record(new Annotation.ClientSend());

		StopWatch.start("ExecuteMethodInterceptor");
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		System.out.println("\n\n\n\nHTTP AFTER");
		Trace.record(new Annotation.ClientRecv(), StopWatch.stopAndGetElapsed("ExecuteMethodInterceptor"));
	}
}