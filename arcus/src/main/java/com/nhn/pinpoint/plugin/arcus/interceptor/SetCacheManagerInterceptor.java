package com.nhn.pinpoint.plugin.arcus.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleBeforeInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class SetCacheManagerInterceptor implements SimpleBeforeInterceptor {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}
		
		String serviceCode = ((ServiceCodeAccessor)args[0]).__getServiceCode();
		((ServiceCodeAccessor)target).__setServiceCode(serviceCode);
	}
}
