package com.profiler.interceptor;

public interface StaticBeforeInterceptor extends Interceptor {
    void before(Object target, String className, String methodName, Object[] args);
}
