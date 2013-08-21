package com.nhn.pinpoint.profiler.modifier.method.interceptor;

import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.*;

/**
 * 
 * @author netspider
 * 
 */
public class MethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, ServiceTypeSupport, TraceContextSupport {
    // method intereptor는 객체의 라이프 사이클을 알수 없이 자주 호출될수 있으므로 그냥 static으로 선언한다.
	private static final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class.getName());
    private static final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;
    private ServiceType serviceType = ServiceType.INTERNAL_METHOD;


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

        trace.recordServiceType(serviceType);
	}

	@Override
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
            logger.afterInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

        try {
            trace.recordApi(descriptor);
            trace.recordException(result);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
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
