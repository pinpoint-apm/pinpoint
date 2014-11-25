package com.nhn.pinpoint.plugin.arcus.interceptor;

import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.plugin.arcus.accessor.OperationAccessor;


/**
 * @author harebox
 * @author emeroad
 */
public class FutureSetOperationInterceptor implements SimpleAroundInterceptor {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		((OperationAccessor)target).__setOperation((Operation) args[0]);
	}

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // do nothing
    }
}
