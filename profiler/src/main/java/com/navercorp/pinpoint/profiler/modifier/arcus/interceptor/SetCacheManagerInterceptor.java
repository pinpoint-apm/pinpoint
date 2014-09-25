package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import net.spy.memcached.CacheManager;
import net.spy.memcached.MemcachedClient;


import com.nhn.pinpoint.bootstrap.util.MetaObject;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class SetCacheManagerInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
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

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}
