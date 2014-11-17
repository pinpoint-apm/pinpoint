package com.nhn.pinpoint.bootstrap.interceptor;

public interface SimpleBeforeInterceptor extends Interceptor {
    void before(Object target, Object[] args);
}
