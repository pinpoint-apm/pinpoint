package com.profiler.modifier.arcus.interceptors;

import com.profiler.interceptor.SimpleBeforeInterceptor;
import com.profiler.logging.Logger;

import com.profiler.logging.LoggerFactory;
import net.spy.memcached.CacheManager;
import net.spy.memcached.MemcachedClient;

import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;

/**
 * 
 * @author netspider
 * 
 */
public class SetCacheManagerInterceptor implements SimpleBeforeInterceptor {

	private final Logger logger = LoggerFactory.getLogger(SetCacheManagerInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<String> getServiceCode = new MetaObject<String>("__getServiceCode");
	private MetaObject<String> setServiceCode = new MetaObject<String>("__setServiceCode", String.class);

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}
		
		CacheManager cm = (CacheManager) args[0];
		String serviceCode = getServiceCode.invoke(cm);
		
		setServiceCode.invoke((MemcachedClient) target, serviceCode);
	}
}
