package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.MetaObject;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class CacheManagerConstructInterceptor implements SimpleAroundInterceptor {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<Object> setServiceCode = new MetaObject<Object>("__setServiceCode", String.class);

    @Override
    public void before(Object target, Object[] args) {

    }

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
		}

		setServiceCode.invoke(target, (String) args[1]);
	}
}
