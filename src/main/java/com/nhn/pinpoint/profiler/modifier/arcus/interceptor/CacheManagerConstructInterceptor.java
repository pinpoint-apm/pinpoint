package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.logging.PLogger;

import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;

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
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
            logger.afterInterceptor(target, args, result);
		}

		setServiceCode.invoke(target, (String) args[1]);
	}
}
