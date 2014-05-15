package com.nhn.pinpoint.profiler.modifier.orm;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.common.ServiceType;

/**
 * @author Hyun Jeong
 */
public abstract class SqlMapOperationInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final ServiceType serviceType;
	
	private MethodDescriptor descriptor;
	private TraceContext traceContext;
	
	public SqlMapOperationInterceptor(ServiceType serviceType) {
		this.serviceType = serviceType;
	}
	
	protected abstract PLogger getLogger();
	
	@Override
	public final void before(Object target, Object[] args) {
		if (getLogger().isDebugEnabled()) {
			getLogger().beforeInterceptor(target, args);
		}
		
		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
		trace.traceBlockBegin();
		trace.markBeforeTime();
	}

	@Override
	public final void after(Object target, Object[] args, Object result) {
		if (getLogger().isDebugEnabled()) {
			getLogger().afterInterceptor(target, args, result);
		}
		
		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
		try {
			trace.recordServiceType(this.serviceType);
			trace.recordException(result);
			if (args != null && args.length > 0) {
				trace.recordApi(descriptor, args[0], 0);
			} else {
				trace.recordApi(descriptor);
			}
			trace.markAfterTime();
		} finally {
			trace.traceBlockEnd();
		}
	}

	@Override
	public final void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
	}

	@Override
	public final void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
	}
}
