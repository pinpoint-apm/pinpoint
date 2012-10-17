package com.profiler.modifier.tomcat.interceptors;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Header;
import com.profiler.context.SpanID;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.context.TraceID;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.NumberUtils;
import com.profiler.util.StringUtils;

public class StandardHostValveInvokeInterceptor implements StaticAroundInterceptor {
    private final Logger logger = Logger.getLogger(StandardHostValveInvokeInterceptor.class.getName());

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
		try {
            TraceContext traceContext = TraceContext.getTraceContext();
            traceContext.getActiveThreadCounter().start();

            HttpServletRequest request = (HttpServletRequest) args[0];
			String requestURL = request.getRequestURI();
			String clientIP = request.getRemoteAddr();
			String parameters = getRequestParameter(request);

			TraceID traceId = populateTraceIdFromRequest(request);
			if (traceId != null) {
				Trace.setTraceId(traceId);
			} else {
                TraceID newTraceID = TraceID.newTraceId();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("TraceID not exist. start new trace. " + newTraceID);
                    // 좀더 자세한 정보는 debug레벨로
                    logger.log(Level.FINE, "requestUrl:" + requestURL + " clientIp" + clientIP + " parameter:" + parameters);
                }
				Trace.setTraceId(newTraceID);
			}
			
			Trace.recordRpcName("TOMCAT", requestURL);
			Trace.recordEndPoint(request.getProtocol() + ":" + request.getLocalName() + ":" + request.getLocalPort());
			Trace.recordAttibute("http.url", request.getRequestURI());
			if (!org.apache.commons.lang.StringUtils.isEmpty(parameters)) {
				Trace.recordAttibute("http.params", parameters);
			}
			Trace.record(Annotation.ServerRecv);

			StopWatch.start("StandardHostValveInvokeInterceptor-starttime");
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, "Tomcat StandardHostValve trace start fail", e);
            }
		}
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }

        TraceContext traceContext = TraceContext.getTraceContext();
        traceContext.getActiveThreadCounter().end();

		// TODO result 가 Exception 타입일경우 호출 실패임.
		Trace.record(Annotation.ServerSend, StopWatch.stopAndGetElapsed("StandardHostValveInvokeInterceptor-starttime"));
//		RequestTracer.endTransaction();
		
		// TODO: I'v changed point of removing. Trace.mutate()
		// Trace.removeTraceId();
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
			if (logger.isLoggable(Level.INFO)) {
			    logger.info("TraceID exist. continue trace. " + id);
            }
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
