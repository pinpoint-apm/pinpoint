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
    // method intereptor는 객체의 라이프 사이클을 알수 없이 자주 호출될수 있으므로 그냥 static으로 선언한다.
	private static final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class.getName());
    private static final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;
    private ServiceType serviceType = ServiceType.INTERNAL_METHOD;


	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, className, methodName, parameterDescription, args);
		}

		Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();

        trace.recordServiceType(serviceType);
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (isDebug) {
            logger.afterInterceptor(target, className, methodName, parameterDescription, args);
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
