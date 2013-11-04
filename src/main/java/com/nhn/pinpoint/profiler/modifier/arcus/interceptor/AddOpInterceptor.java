package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TargetClassLoader;
import com.nhn.pinpoint.profiler.logging.PLogger;

import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.profiler.util.MetaObject;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class AddOpInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<String> getServiceCode = new MetaObject<String>("__getServiceCode");
	private MetaObject<String> setServiceCode = new MetaObject<String>("__setServiceCode", String.class);

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
            logger.beforeInterceptor(target, args);
		}

		String serviceCode = getServiceCode.invoke((MemcachedClient) target);
		Operation op = (Operation) args[1];

		setServiceCode.invoke(op, serviceCode);
	}

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
    }
}
