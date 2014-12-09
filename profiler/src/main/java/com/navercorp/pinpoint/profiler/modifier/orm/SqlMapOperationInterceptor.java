package com.navercorp.pinpoint.profiler.modifier.orm;

import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public abstract class SqlMapOperationInterceptor extends SpanEventSimpleAroundInterceptor {

	private final ServiceType serviceType;

	public SqlMapOperationInterceptor(ServiceType serviceType, Class<? extends SpanEventSimpleAroundInterceptor> childClazz) {
        super(childClazz);
		this.serviceType = serviceType;
	}
	
	@Override
	public final void doInBeforeTrace(RecordableTrace trace, final Object target, Object[] args) {
		trace.markBeforeTime();
	}

	@Override
	public final void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordServiceType(this.serviceType);
        trace.recordException(throwable);
        if (args != null && args.length > 0) {
            trace.recordApiCachedString(getMethodDescriptor(), (String)args[0], 0);
        } else {
            trace.recordApi(getMethodDescriptor());
        }
        trace.markAfterTime();
	}

}
