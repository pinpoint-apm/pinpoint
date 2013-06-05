package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;


/**
 * @author harebox
 */
public class FutureSetOperationInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(FutureSetOperationInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private MetaObject<Object> setOperation = new MetaObject<Object>("__setOperation", Operation.class);
    
    private MethodDescriptor methodDescriptor;
    private TraceContext traceContext;

    @Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		setOperation.invoke(target, (Operation) args[0]);
	}

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.methodDescriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
    }
}
