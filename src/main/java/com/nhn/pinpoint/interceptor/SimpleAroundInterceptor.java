package com.nhn.pinpoint.interceptor;

/**
 *
 */
public interface SimpleAroundInterceptor extends Interceptor {

    void before(Object target, Object[] args);

    void after(Object target, Object[] args, Object result);
}
