package com.nhn.pinpoint.profiler.modifier.orm.mybatis.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;

/**
 * @author Hyun Jeong
 */
public class SqlSessionInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {
	
	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();
	
	private MethodDescriptor descriptor;
	private TraceContext traceContext;

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
	}

	@Override
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
			logger.afterInterceptor(target, args, result);
		}
		
		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
		try {
			trace.recordServiceType(ServiceType.MYBATIS);
			trace.recordException(result);
			if (args != null && args.length > 0) {
				trace.recordApi(descriptor, args[0], 0);
			} else {
				trace.recordApi(descriptor);
			}
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
