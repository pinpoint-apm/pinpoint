package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.bootstrap.context.AsyncTrace;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

import com.nhn.pinpoint.profiler.context.DefaultAsyncTrace;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.TimeObject;
import net.spy.memcached.protocol.BaseOperationImpl;

import com.nhn.pinpoint.bootstrap.util.MetaObject;

/**
 * @author emeroad
 */
public class BaseOperationCancelInterceptor implements SimpleAroundInterceptor {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
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
