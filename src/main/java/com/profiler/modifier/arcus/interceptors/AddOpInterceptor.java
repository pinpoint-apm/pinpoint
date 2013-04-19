package com.profiler.modifier.arcus.interceptors;

import com.profiler.logging.Logger;

import com.profiler.logging.LoggerFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ops.Operation;

import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;

/**
 * 
 * @author netspider
 * 
 */
public class AddOpInterceptor implements StaticBeforeInterceptor {

	private final Logger logger = LoggerFactory.getLogger(AddOpInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<String> getServiceCode = new MetaObject<String>("__getServiceCode");
	private MetaObject<String> setServiceCode = new MetaObject<String>("__setServiceCode", String.class);

	private final String MEMCACHED = "MEMCACHED";

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (isDebug) {
            logger.beforeInterceptor(target, className, methodName, parameterDescription, args);
		}

		String serviceCode = getServiceCode.invoke((MemcachedClient) target);
		Operation op = (Operation) args[1];

		if (target instanceof MemcachedClient) {
			if (serviceCode == null)
				serviceCode = MEMCACHED;
		}

		setServiceCode.invoke(op, serviceCode);
	}
}
