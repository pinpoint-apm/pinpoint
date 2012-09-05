package com.profiler.modifier.tomcat.interceptors;

import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Header;
import com.profiler.context.SpanID;
import com.profiler.context.Trace;
import com.profiler.context.TraceID;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.trace.RequestTracer;
import com.profiler.util.NumberUtils;

public class InvokeMethodInterceptor implements StaticAroundInterceptor {

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		try {
			HttpServletRequest request = (HttpServletRequest) args[0];
			String requestURL = request.getRequestURI();
			String clientIP = request.getRemoteAddr();
			String parameters = getRequestParameter(request);

			TraceID traceId = populateTraceIdFromRequest(request);
			if (traceId != null) {
				Trace.setTraceId(traceId);
			} else {
				System.out.println(requestURL);
				System.out.println(clientIP);
				System.out.println(parameters);
				
				Trace.setTraceId(TraceID.newTraceId());
			}

			Trace.recordRpcName("tomcat", requestURL);
			Trace.recordEndPoint(request.getLocalAddr(), request.getLocalPort());
			Trace.recordAttibute("http.params", parameters);
			Trace.record(Annotation.ServerRecv);

			RequestTracer.startTransaction(requestURL, clientIP, System.currentTimeMillis(), parameters);

			StopWatch.start("InvokeMethodInterceptor-starttime");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		// TODO result 가 Exception 타입일경우 호출 실패임.
		Trace.record(Annotation.ServerSend, StopWatch.stopAndGetElapsed("InvokeMethodInterceptor-starttime"));
		RequestTracer.endTransaction();
	}

	/**
	 * Pupulate source trace from HTTP Header.
	 * 
	 * @param request
	 * @return
	 */
	private TraceID populateTraceIdFromRequest(HttpServletRequest request) {
		String strUUID = request.getHeader(Header.HTTP_TRACE_ID.toString());

		if (strUUID != null) {
			UUID uuid = UUID.fromString(strUUID);
			long parentSpanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanID.NULL);
			long spanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanID.NULL);
			boolean sampled = Boolean.parseBoolean(request.getHeader(Header.HTTP_SAMPLED.toString()));
			int flags = NumberUtils.parseInteger(request.getHeader(Header.HTTP_FLAGS.toString()), 0);

			TraceID id = new TraceID(uuid, parentSpanID, spanID, sampled, flags);

			// TODO : remove this, just for debug
			System.out.println("\nGOT A TRACEID. TRACEID=" + id + "\n\n");

			return id;
		} else {
			return null;
		}
	}

	private String getRequestParameter(HttpServletRequest request) {
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
