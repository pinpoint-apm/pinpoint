package com.nhn.pinpoint.plugin.arcus.interceptor;


import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class AddOpInterceptor implements SimpleAroundInterceptor {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
            logger.beforeInterceptor(target, args);
		}

		String serviceCode = ((ServiceCodeAccessor)target).__getServiceCode();
		((ServiceCodeAccessor)args[1]).__setServiceCode(serviceCode);
	}

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }
}
