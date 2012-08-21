package com.profiler.interceptor;

public interface Interceptor {
    void before(InterceptorContext ctx);
    void after(InterceptorContext ctx);
}
