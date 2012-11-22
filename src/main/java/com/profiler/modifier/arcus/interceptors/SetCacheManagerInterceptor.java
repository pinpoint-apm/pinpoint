package com.profiler.modifier.arcus.interceptors;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.spy.memcached.CacheManager;
import net.spy.memcached.MemcachedClient;

import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

/**
 * 
 * @author netspider
 * 
 */
public class SetCacheManagerInterceptor implements StaticBeforeInterceptor {

	private final Logger logger = Logger.getLogger(SetCacheManagerInterceptor.class.getName());

	private MetaObject<String> getServiceCode = new MetaObject<String>("__getServiceCode");
	private MetaObject<String> setServiceCode = new MetaObject<String>("__setServiceCode", String.class);

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
		}
		
		CacheManager cm = (CacheManager) args[0];
		String serviceCode = getServiceCode.invoke(cm);
		
		System.out.println("[HIPPO-SET_CACHE_MAN_FUNC] GET SERVICE_CODE FROM CM=" + serviceCode);
		
		setServiceCode.invoke((MemcachedClient) target, serviceCode);
	}
}
