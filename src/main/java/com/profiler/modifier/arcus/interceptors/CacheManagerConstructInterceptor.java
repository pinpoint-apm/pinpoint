package com.profiler.modifier.arcus.interceptors;

import com.profiler.interceptor.SimpleAfterInterceptor;
import com.profiler.logging.Logger;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;

/**
 * 
 * @author netspider
 * 
 */
public class CacheManagerConstructInterceptor implements SimpleAfterInterceptor {

	private final Logger logger = LoggerFactory.getLogger(CacheManagerConstructInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<Object> setServiceCode = new MetaObject<Object>("__setServiceCode", String.class);

	@Override
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
			logger.afterInterceptor(target, args, result);
		}

		setServiceCode.invoke(target, (String) args[1]);
	}
}
