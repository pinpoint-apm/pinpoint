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
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		System.out.println("\n\n\n\nINVOKE HTTP START ----------------------------------------------------------------------------------------------------------------------------------------------------");

		final HttpHost host = (HttpHost) args[0];
		final HttpRequest request = (HttpRequest) args[1];

		try {
			Trace.traceBlockBegin();
			TraceID nextId = Trace.getNextTraceId();

			// UUID format을 그대로.
			request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
			request.addHeader(Header.HTTP_SPAN_ID.toString(), Long.toString(nextId.getSpanId()));
			request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Long.toString(nextId.getParentSpanId()));
			request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
			request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));

			Trace.recordRpcName("http-call", "");
			Trace.recordEndPoint("http", host.getHostName(), host.getPort());
			Trace.recordAttibute("http.url", request.toString());
			Trace.record(Annotation.ClientSend);
		} finally {
			Trace.traceBlockEnd();
		}

		StopWatch.start("ExecuteMethodInterceptor");
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		Trace.traceBlockBegin();
		Trace.record(Annotation.ClientRecv, StopWatch.stopAndGetElapsed("ExecuteMethodInterceptor"));
		Trace.traceBlockEnd();

		System.out.println("\n\n\n\nINVOKE HTTP END ----------------------------------------------------------------------------------------------------------------------------------------------------");
	}
}