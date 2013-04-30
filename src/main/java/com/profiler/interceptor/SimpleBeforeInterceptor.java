package com.profiler.interceptor;

/**
 *
 */
public interface SimpleBeforeInterceptor extends Interceptor {
    void before(Object target, Object[] args);
}
