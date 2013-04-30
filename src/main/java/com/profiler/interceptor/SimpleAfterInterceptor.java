package com.profiler.interceptor;

/**
 *
 */
public interface SimpleAfterInterceptor extends Interceptor {
    void after(Object target, Object[] args, Object result);
}
