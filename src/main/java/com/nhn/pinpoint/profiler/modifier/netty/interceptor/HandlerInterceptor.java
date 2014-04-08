package com.nhn.pinpoint.profiler.modifier.netty.interceptor;

import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.*;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class HandlerInterceptor implements SimpleAroundInterceptor, ServiceTypeSupport, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final PLogger logger = PLoggerFactory.getLogger(HandlerInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();

	private MethodDescriptor descriptor;
	private TraceContext traceContext;
    private ServiceType serviceType = ServiceType.INTERNAL_METHOD;


	@Override
	public void before(Object target, Object[] args) {
		System.out.println("before run()");
		System.out.println("\ttarget cl=" + target.getClass().getClassLoader());
		System.out.println("\ttarget pcl=" + target.getClass().getClassLoader().getParent());
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
		System.out.println("after run()");
		System.out.println("\ttarget cl=" + target.getClass().getClassLoader());
		System.out.println("\ttarget pcl=" + target.getClass().getClassLoader().getParent());
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
