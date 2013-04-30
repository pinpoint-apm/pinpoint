package com.profiler.modifier.arcus.interceptors;

import com.profiler.context.AsyncTrace;
import com.profiler.interceptor.SimpleAroundInterceptor;
import com.profiler.logging.Logger;

import com.profiler.context.DefaultAsyncTrace;
import com.profiler.logging.LoggerFactory;
import com.profiler.util.TimeObject;
import net.spy.memcached.protocol.BaseOperationImpl;

import com.profiler.util.MetaObject;

/**
 *
 */
public class BaseOperationCancelInterceptor implements SimpleAroundInterceptor {

	private final Logger logger = LoggerFactory.getLogger(BaseOperationCancelInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject getAsyncTrace = new MetaObject("__getAsyncTrace");

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		AsyncTrace asyncTrace = (AsyncTrace) getAsyncTrace.invoke(target);
		if (asyncTrace == null) {
			logger.debug("asyncTrace not found ");
			return;
		}

		if (asyncTrace.getState() != DefaultAsyncTrace.STATE_INIT) {
			// 이미 동작 완료된 상태임.
			return;
		}

		BaseOperationImpl baseOperation = (BaseOperationImpl) target;
		if (!baseOperation.isCancelled()) {
			TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
			timeObject.markCancelTime();
		}
	}

    @Override
    public void after(Object target, Object[] args, Object result) {

    }
}
