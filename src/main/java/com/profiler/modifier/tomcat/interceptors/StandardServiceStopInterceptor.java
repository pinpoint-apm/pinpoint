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
public class StandardServiceStopInterceptor implements SimpleAfterInterceptor {

    private final Logger logger = LoggerFactory.getLogger(StandardServiceStopInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private LifeCycleEventListener lifeCycleEventListener;

    public StandardServiceStopInterceptor(LifeCycleEventListener lifeCycleEventListener) {
        this.lifeCycleEventListener = lifeCycleEventListener;
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
		// TODO 시작이 실패했을때 stop이 불러 지는가?
		// if (!InterceptorUtils.isSuccess(result)) {
		// return;
		// }
        lifeCycleEventListener.stop();
    }
}
