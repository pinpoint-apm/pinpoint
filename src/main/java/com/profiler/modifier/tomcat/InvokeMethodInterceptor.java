package com.profiler.modifier.tomcat;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.profiler.context.Header;
import com.profiler.context.RequestContext;
import com.profiler.context.Trace;
import com.profiler.context.TraceID;
import com.profiler.context.gen.Annotation;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.trace.RequestTracer;

public class InvokeMethodInterceptor implements StaticAroundInterceptor {

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		System.out.println("\n\n\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("\n\n\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("\n\n\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		System.out.println("interceptor=" + InvokeMethodInterceptor.class.getClassLoader());

        try {
            HttpServletRequest request = (HttpServletRequest) args[0];
            String requestURL = request.getRequestURI();
            String clientIP = request.getRemoteAddr();
            String traceID = request.getHeader(Header.HTTP_TRACE_ID.toString());
            String parentSpanID = request.getHeader(Header.HTTP_TRACE_PARENT_SPAN_ID.toString());
            Boolean debug = Boolean.valueOf(request.getHeader(Header.HTTP_TRACE_DEBUG.toString()));
            String parameters = getParameter(request);

            if (traceID == null)
                traceID = TraceID.newTraceID();

            Trace trace = RequestContext.getTrace(traceID, parentSpanID, "StandardHostValveInterceptor", debug);

            Annotation a = new Annotation();
            a.setTimestamp(System.currentTimeMillis());

            RequestTracer.startTransaction(requestURL, clientIP, System.currentTimeMillis(), parameters);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
