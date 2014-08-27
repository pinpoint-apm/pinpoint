package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.bootstrap.util.MetaObject;


/**
 * @author harebox
 * @author emeroad
 */
public class FutureSetOperationInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MetaObject<Object> setOperation = new MetaObject<Object>("__setOperation", Operation.class);
    

    @Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		setOperation.invoke(target, (Operation) args[0]);
	}

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
