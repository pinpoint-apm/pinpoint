package com.profiler.modifier.method.interceptors;

import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import com.profiler.common.ServiceType;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.*;
import com.profiler.logging.LoggingUtils;

/**
 * 
 * @author netspider
 * 
 */
public class MethodInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport, ServiceTypeSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;
    private ServiceType serviceType = ServiceType.INTERNAL_METHOD;

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (isDebug) {
			LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

		trace.traceBlockBegin();
        trace.recordServiceType(serviceType);
//        trace.recordRpcName(ServiceType.INTERNAL_METHOD, null, null);
		trace.markBeforeTime();
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}
        
		trace.recordApi(descriptor);
		trace.recordException(result);
		trace.markAfterTime();
		trace.traceBlockEnd();
	}

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    @Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
    }
}
