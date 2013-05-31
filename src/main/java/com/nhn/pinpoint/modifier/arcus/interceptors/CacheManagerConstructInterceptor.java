package com.nhn.pinpoint.modifier.arcus.interceptors;

import com.nhn.pinpoint.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.logging.Logger;

import com.nhn.pinpoint.logging.LoggerFactory;
import com.nhn.pinpoint.util.MetaObject;

/**
 * 
 * @author netspider
 * 
 */
public class CacheManagerConstructInterceptor implements SimpleAroundInterceptor {

	private final Logger logger = LoggerFactory.getLogger(CacheManagerConstructInterceptor.class.getName());
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
