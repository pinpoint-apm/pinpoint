package com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * suitable method
 * <pre>
 * org.apache.http.concurrent.BasicFuture.completed(T)
 * </pre>
 *
 * original code of method
 * <code>
 * <pre>
 * 	public boolean completed(final T result) {
 * 		synchronized (this) {
 * 			if (this.completed) {
 * 				return false;
 * 			}
 * 			this.completed = true;
 * 			this.result = result;
 * 			notifyAll();
 * 		}
 * 		if (this.callback != null) {
 * 			this.callback.completed(result);
 * 		}
 * 		return true;
 * 	}
 * </pre>
 * </code>
 * 
 * @author netspider
 * 
 */
public class BasicFutureCompletedInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	protected PLogger logger;
	protected boolean isDebug;

	protected TraceContext traceContext;
	protected MethodDescriptor descriptor;

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

		trace.traceBlockBegin();
		trace.markBeforeTime();
		trace.recordServiceType(ServiceType.HTTP_CLIENT_INTERNAL);
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (isDebug) {
			logger.afterInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

		try {
			trace.recordApi(descriptor);
			trace.recordException(throwable);
			trace.markAfterTime();
		} finally {
			trace.traceBlockEnd();
		}
	}

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}
}
