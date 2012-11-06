package com.profiler.modifier.tomcat.interceptors;

import com.profiler.LifeCycleEventListener;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class StandardServiceStopInterceptor implements StaticAfterInterceptor {
    private final Logger logger = Logger.getLogger(StandardServiceStopInterceptor.class.getName());

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        // TODO 시작이 실패했을때 stop이 불러 지는가?
//        if (!InterceptorUtils.isSuccess(result)) {
//            return;
//        }
        LifeCycleEventListener.stop();
    }
}
