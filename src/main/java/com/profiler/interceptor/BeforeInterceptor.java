package com.profiler.interceptor;

public interface BeforeInterceptor extends Interceptor {
     void before(InterceptorContext ctx);
}
