package com.profiler.interceptor;

public interface AroundInterceptor extends BeforeInterceptor, AfterInterceptor {
    @Override
    void before(InterceptorContext ctx);

    @Override
    void after(InterceptorContext ctx);
}
