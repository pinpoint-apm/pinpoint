package com.profiler.modifier.arcus.interceptors;

import com.profiler.logging.Logger;

import com.profiler.context.DefaultAsyncTrace;
import com.profiler.logging.LoggerFactory;
import net.spy.memcached.protocol.BaseOperationImpl;

import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;

/**
 *
 */
public class BaseOperationCancelInterceptor implements StaticBeforeInterceptor {

	private final Logger logger = LoggerFactory.getLogger(BaseOperationCancelInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject getAsyncTrace = new MetaObject("__getAsyncTrace");

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (isDebug) {
			LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
		}

		DefaultAsyncTrace asyncTrace = (DefaultAsyncTrace) getAsyncTrace.invoke(target);
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

}
