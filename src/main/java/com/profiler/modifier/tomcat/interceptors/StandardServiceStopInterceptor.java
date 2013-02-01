package com.profiler.modifier.tomcat.interceptors;

import java.util.Arrays;
import java.util.logging.Logger;

import com.profiler.LifeCycleEventListener;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.StringUtils;

/**
 *
 */
public class StandardServiceStopInterceptor implements StaticAfterInterceptor {

    private final Logger logger = Logger.getLogger(StandardServiceStopInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private LifeCycleEventListener lifeCycleEventListener;

    public StandardServiceStopInterceptor(LifeCycleEventListener lifeCycleEventListener) {
        this.lifeCycleEventListener = lifeCycleEventListener;
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            logger.fine("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
		// TODO 시작이 실패했을때 stop이 불러 지는가?
		// if (!InterceptorUtils.isSuccess(result)) {
		// return;
		// }
        lifeCycleEventListener.stop();
    }
}
