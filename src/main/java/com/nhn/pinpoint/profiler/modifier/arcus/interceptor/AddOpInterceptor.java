package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.logging.Logger;

import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.profiler.util.MetaObject;

/**
 * 
 * @author netspider
 * 
 */
public class AddOpInterceptor implements SimpleAroundInterceptor {

	private final Logger logger = LoggerFactory.getLogger(AddOpInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<String> getServiceCode = new MetaObject<String>("__getServiceCode");
	private MetaObject<String> setServiceCode = new MetaObject<String>("__setServiceCode", String.class);

	private final String MEMCACHED = "MEMCACHED";

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
            logger.beforeInterceptor(target, args);
		}

		String serviceCode = getServiceCode.invoke((MemcachedClient) target);
		Operation op = (Operation) args[1];

		if (target instanceof MemcachedClient) {
			if (serviceCode == null)
				serviceCode = MEMCACHED;
		}

		setServiceCode.invoke(op, serviceCode);
	}

    @Override
    public void after(Object target, Object[] args, Object result) {
    }
}
