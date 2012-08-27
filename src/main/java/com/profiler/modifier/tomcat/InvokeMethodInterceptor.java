package com.profiler.modifier.tomcat;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.profiler.context.Annotation;
import com.profiler.context.Header;
import com.profiler.context.Trace;
import com.profiler.context.TraceID;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.trace.RequestTracer;

public class InvokeMethodInterceptor implements StaticAroundInterceptor {

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		try {
			HttpServletRequest request = (HttpServletRequest) args[0];
			String requestURL = request.getRequestURI();
			String clientIP = request.getRemoteAddr();

			String traceID = request.getHeader(Header.HTTP_TRACE_ID.toString());
			String parentSpanID = request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString());
			String spanID = request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString());
			Boolean sampled = Boolean.valueOf(request.getHeader(Header.HTTP_SAMPLED.toString()));
			Integer flags = Integer.valueOf(request.getHeader(Header.HTTP_FLAGS.toString()));

			String parameters = getParameter(request);

			// record
			// TODO: traceid 유무 확인.
			Trace.setTraceId(new TraceID(traceID, parentSpanID, spanID, sampled, flags));
			Trace.recordRpcName("service_name", requestURL);
			Trace.recordServerAddr(request.getLocalName());
			Trace.recordBinary("http.uri", parameters);
			Trace.record(new Annotation.ServerRecv());

			RequestTracer.startTransaction(requestURL, clientIP, System.currentTimeMillis(), parameters);
		} catch (Exception e) {
			e.printStackTrace();
			// To change body of catch statement use File | Settings | File
			// Templates.
		}
		System.out.println("end--------------");
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		RequestTracer.endTransaction();
	}

	private String getParameter(HttpServletRequest request) {
		Enumeration<?> attrs = request.getParameterNames();

		StringBuilder params = new StringBuilder();

		while (attrs.hasMoreElements()) {
			String keyString = attrs.nextElement().toString();

			System.out.println(request.getParameter(keyString));

			Object value = request.getParameter(keyString);
			if (value != null) {
				String valueString = value.toString();
				int valueStringLength = valueString.length();

				if (valueStringLength > 0 && valueStringLength < 100)
					params.append(keyString).append("=").append(valueString);
			}
		}

		return params.toString();
	}
}
