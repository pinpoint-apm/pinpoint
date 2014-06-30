package com.nhn.pinpoint.bootstrap.interceptor;

/**
 * @author emeroad
 */
public interface SimpleAroundInterceptor extends Interceptor {

    void before(Object target, Object[] args);

    void after(Object target, Object[] args, Object result, Throwable throwable);
}
