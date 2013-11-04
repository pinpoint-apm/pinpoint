package com.nhn.pinpoint.profiler.interceptor;

/**
 * @author emeroad
 */
public interface StaticAroundInterceptor extends Interceptor {

    void before(Object target, String className, String methodName, String parameterDescription, Object[] args);

    void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result);

}
