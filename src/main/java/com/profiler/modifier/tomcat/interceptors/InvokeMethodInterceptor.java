package com.profiler.modifier.tomcat.interceptors;

import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.profiler.context.*;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.trace.RequestTracer;
import com.profiler.util.NamedThreadLocal;
import com.profiler.util.NumberUtils;

public class InvokeMethodInterceptor implements StaticAroundInterceptor {

    private ThreadLocal<Long> start = new NamedThreadLocal<Long>("InvokeMethodInterceptor-starttime");

    @Override
    public void before(Object target, String className, String methodName, Object[] args) {
        try {
            HttpServletRequest request = (HttpServletRequest) args[0];
            String requestURL = request.getRequestURI();
            String clientIP = request.getRemoteAddr();
            String parameters = getParameter(request);

            UUID traceID = getTraceId(request);
            if (traceID != null) {
                long parentSpanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanID.NULL);
                long spanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanID.NULL);
                boolean sampled = Boolean.parseBoolean(request.getHeader(Header.HTTP_SAMPLED.toString()));
                int flags = NumberUtils.parseInteger(request.getHeader(Header.HTTP_FLAGS.toString()), 0);

				TraceID id = new TraceID(traceID, parentSpanID, spanID, sampled, flags);

				// TODO : refactor this, just for debug
				System.out.println("\n\n\ngot a traceid. traceid=" + id + "\n\n\n");

				Trace.setTraceId(id);
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
        // TODO result 가 Exception 타입일경우 호출 실패임.
        Trace.record(new Annotation.ServerSend(), System.currentTimeMillis() - start.get());
        start.remove();
        RequestTracer.endTransaction();
    }
    
    private UUID getTraceId(HttpServletRequest request) {
        String header = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (header == null) {
            return null;
        }
        try {
            return UUID.fromString(header);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
