package com.profiler.modifier.connector.jdkhttpconnector.interceptor;

import java.net.HttpURLConnection;
import java.util.logging.Logger;

import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.context.Header;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.context.TraceID;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggingUtils;

/**
 * @author netspider
 * 
 */
public class ConnectMethodInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

	private final Logger logger = Logger.getLogger(ConnectMethodInterceptor.class.getName());
	private final boolean isDebug = LoggingUtils.isDebug(logger);

	private MethodDescriptor descriptor;

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (isDebug) {
			LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
		}
		TraceContext traceContext = TraceContext.getTraceContext();
		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
		trace.traceBlockBegin();
		trace.markBeforeTime();

		TraceID nextId = trace.getTraceId().getNextTraceId();
		trace.recordNextSpanId(nextId.getSpanId());

		HttpURLConnection request = (HttpURLConnection) target;

		// UUID format을 그대로.
		request.setRequestProperty(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
		request.setRequestProperty(Header.HTTP_SPAN_ID.toString(), Integer.toString(nextId.getSpanId()));
		request.setRequestProperty(Header.HTTP_PARENT_SPAN_ID.toString(), Integer.toString(nextId.getParentSpanId()));
		request.setRequestProperty(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
		request.setRequestProperty(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
		request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationId());
		request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), String.valueOf(ServiceType.TOMCAT.getCode()));

		trace.recordServiceType(ServiceType.JDK_HTTPURLCONNECTOR);

		String host = request.getURL().getHost();
		int port = request.getURL().getPort();

		// TODO protocol은 어떻게 표기하지???
//		trace.recordEndPoint(host + ((port > 0) ? ":" + port : ""));
		trace.recordDestinationId(host + ((port > 0) ? ":" + port : ""));

		trace.recordAttribute(AnnotationKey.HTTP_URL, request.getURL().toString());
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (isDebug) {
			// result는 로깅하지 않는다.
			LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args);
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
		TraceContext traceContext = TraceContext.getTraceContext();
		traceContext.cacheApi(descriptor);
	}
}