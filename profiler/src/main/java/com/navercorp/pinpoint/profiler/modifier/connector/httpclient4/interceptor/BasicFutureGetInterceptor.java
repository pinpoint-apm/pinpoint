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
 * suitable method
 * 
 * <pre>
 * org.apache.http.concurrent.BasicFuture.get()
 * org.apache.http.concurrent.BasicFuture.get(long, TimeUnit)
 * </pre>
 * 
 * <code>
 * <pre>
 * 	public synchronized T get() throws InterruptedException, ExecutionException {
 * 		while (!this.completed) {
 * 			wait();
 * 		}
 * 		return getResult();
 * 	}
 * 
 * 	public synchronized T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
 * 		Args.notNull(unit, "Time unit");
 * 		final long msecs = unit.toMillis(timeout);
 * 		final long startTime = (msecs <= 0) ? 0 : System.currentTimeMillis();
 * 		long waitTime = msecs;
 * 		if (this.completed) {
 * 			return getResult();
 * 		} else if (waitTime <= 0) {
 * 			throw new TimeoutException();
 * 		} else {
 * 			for (;;) {
 * 				wait(waitTime);
 * 				if (this.completed) {
 * 					return getResult();
 * 				} else {
 * 					waitTime = msecs - (System.currentTimeMillis() - startTime);
 * 					if (waitTime <= 0) {
 * 						throw new TimeoutException();
 * 					}
 * 				}
 * 			}
 * 		}
 * 	}
 * </pre>
 * </code>
 * 
 * @author netspider
 * 
 */
public class BasicFutureGetInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

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
