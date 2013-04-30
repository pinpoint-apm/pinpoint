package com.profiler.modifier.tomcat.interceptors;

import com.profiler.interceptor.SimpleAfterInterceptor;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import com.profiler.LifeCycleEventListener;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.logging.LoggingUtils;

/**
 *
 */
public class StandardServiceStartInterceptor implements SimpleAfterInterceptor {

    private final Logger logger = LoggerFactory.getLogger(StandardServiceStartInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private LifeCycleEventListener lifeCycleEventListener;

    public StandardServiceStartInterceptor(LifeCycleEventListener lifeCycleEventListener) {
        this.lifeCycleEventListener = lifeCycleEventListener;
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
