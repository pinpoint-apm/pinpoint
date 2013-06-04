package com.nhn.pinpoint.modifier.tomcat.interceptors;

import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;

import com.nhn.pinpoint.LifeCycleEventListener;

/**
 *
 */
public class StandardServiceStopInterceptor implements SimpleAroundInterceptor {

    private final Logger logger = LoggerFactory.getLogger(StandardServiceStopInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private LifeCycleEventListener lifeCycleEventListener;

    public StandardServiceStopInterceptor(LifeCycleEventListener lifeCycleEventListener) {
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
		// TODO 시작이 실패했을때 stop이 불러 지는가?
		// if (!InterceptorUtils.isSuccess(result)) {
		// return;
		// }
        lifeCycleEventListener.stop();
    }
}
