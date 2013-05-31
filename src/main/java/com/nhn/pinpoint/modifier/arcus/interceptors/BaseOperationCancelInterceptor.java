package com.nhn.pinpoint.modifier.arcus.interceptors;

import com.nhn.pinpoint.context.AsyncTrace;
import com.nhn.pinpoint.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.logging.Logger;

import com.nhn.pinpoint.context.DefaultAsyncTrace;
import com.nhn.pinpoint.logging.LoggerFactory;
import com.nhn.pinpoint.util.TimeObject;
import net.spy.memcached.protocol.BaseOperationImpl;

import com.nhn.pinpoint.util.MetaObject;

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
