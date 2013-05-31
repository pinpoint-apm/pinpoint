package com.nhn.pinpoint.modifier.tomcat.interceptors;

import com.nhn.pinpoint.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.logging.Logger;
import com.nhn.pinpoint.logging.LoggerFactory;

import com.nhn.pinpoint.LifeCycleEventListener;

/**
 *
 */
public class StandardServiceStartInterceptor implements SimpleAroundInterceptor {

    private final Logger logger = LoggerFactory.getLogger(StandardServiceStartInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private LifeCycleEventListener lifeCycleEventListener;

    public StandardServiceStartInterceptor(LifeCycleEventListener lifeCycleEventListener) {
        this.lifeCycleEventListener = lifeCycleEventListener;
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
		// if (!InterceptorUtils.isSuccess(result)) {
		// return;
		// }
        lifeCycleEventListener.start();
    }
}
