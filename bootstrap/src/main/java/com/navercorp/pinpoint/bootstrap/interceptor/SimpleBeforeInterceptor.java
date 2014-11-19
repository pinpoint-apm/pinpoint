package com.nhn.pinpoint.bootstrap.interceptor;

public interface SimpleBeforeInterceptor extends SimpleInterceptor {
    void before(Object target, Object[] args);
}
