package com.profiler.modifier.arcus.interceptors;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

/**
 * 
 * @author netspider
 * 
 */
public class CacheManagerConstructInterceptor implements StaticAfterInterceptor {

	private final Logger logger = Logger.getLogger(CacheManagerConstructInterceptor.class.getName());
	private MetaObject<Object> setServiceCode = new MetaObject<Object>("__setServiceCode", String.class);

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
		}

		setServiceCode.invoke(target, (String) args[1]);
	}
}
