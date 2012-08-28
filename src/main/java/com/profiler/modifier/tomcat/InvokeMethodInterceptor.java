package com.profiler.modifier.tomcat;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.profiler.context.Annotation;
import com.profiler.context.Header;
import com.profiler.context.Trace;
import com.profiler.context.TraceID;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.trace.RequestTracer;
import com.profiler.util.NamedThreadLocal;

public class InvokeMethodInterceptor implements StaticAroundInterceptor {

	private ThreadLocal<Long> start = new NamedThreadLocal<Long>("InvokeMethodInterceptor-starttime");

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		try {
			HttpServletRequest request = (HttpServletRequest) args[0];
			String requestURL = request.getRequestURI();
			String clientIP = request.getRemoteAddr();

			String traceID = request.getHeader(Header.HTTP_TRACE_ID.toString());
			String parentSpanID = request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString());
			String spanID = request.getHeader(Header.HTTP_SPAN_ID.toString());

			Boolean sampled = null;
			if (request.getHeader(Header.HTTP_SAMPLED.toString()) != null) {
				sampled = Boolean.valueOf(request.getHeader(Header.HTTP_SAMPLED.toString()));
			}

			Integer flags = null;
			if (request.getHeader(Header.HTTP_FLAGS.toString()) != null) {
				flags = Integer.valueOf(request.getHeader(Header.HTTP_FLAGS.toString()));
			}

			String parameters = getParameter(request);

			// record
			if (traceID != null) {
				Trace.setTraceId(new TraceID(traceID, parentSpanID, spanID, sampled, flags));
			} else {
				Trace.setTraceId(TraceID.newTraceId());
			}

			Trace.recordRpcName("tomcat", requestURL);
			Trace.recordServerAddr(request.getLocalAddr(), request.getLocalPort());
			Trace.record("Parameter=" + parameters);
			Trace.record(new Annotation.ServerRecv());

			RequestTracer.startTransaction(requestURL, clientIP, System.currentTimeMillis(), parameters);

			start.set(System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		Trace.record(new Annotation.ServerSend(), System.currentTimeMillis() - start.get());
		start.remove();
		RequestTracer.endTransaction();
	}

	private String getParameter(HttpServletRequest request) {
		Enumeration<?> attrs = request.getParameterNames();

		StringBuilder params = new StringBuilder();

		while (attrs.hasMoreElements()) {
			String keyString = attrs.nextElement().toString();
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
