package com.profiler.modifier.method.interceptors;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.ServiceType;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.ApiIdSupport;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.util.StringUtils;

/**
 * 
 * @author netspider
 * 
 */
public class MethodInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = Logger.getLogger(MethodInterceptor.class.getName());
	private MethodDescriptor descriptor;
	private TraceContext traceContext;

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
		}

		Trace trace = TraceContext.getTraceContext().currentTraceObject();
		if (trace == null) {
			return;
		}

		trace.traceBlockBegin();
		trace.recordRpcName(ServiceType.INTERNAL_METHOD, null, null);
		trace.markBeforeTime();
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
		}

		Trace trace = TraceContext.getTraceContext().currentTraceObject();
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

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
    }
}
