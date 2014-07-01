package com.nhn.pinpoint.profiler.modifier.orm;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.common.ServiceType;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public abstract class SqlMapOperationInterceptor extends SpanEventSimpleAroundInterceptor {

	private final ServiceType serviceType;

	public SqlMapOperationInterceptor(ServiceType serviceType, PLogger logger) {
        super(logger);
		this.serviceType = serviceType;
	}
	
	@Override
	public final void doInBeforeTrace(Trace trace, final Object target, Object[] args) {
		trace.markBeforeTime();
	}

	@Override
	public final void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordServiceType(this.serviceType);
        trace.recordException(throwable);
        if (args != null && args.length > 0) {
            trace.recordApi(getMethodDescriptor(), args[0], 0);
        } else {
            trace.recordApi(getMethodDescriptor());
        }
        trace.markAfterTime();
	}

}
