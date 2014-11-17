package com.nhn.pinpoint.bootstrap.interceptor;

/**
 * @author emeroad
 */
public interface SimpleAfterInterceptor extends Interceptor {
    void after(Object target, Object[] args, Object result, Throwable throwable);
}
