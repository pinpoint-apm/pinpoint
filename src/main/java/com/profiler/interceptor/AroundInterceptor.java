package com.profiler.interceptor;

@Deprecated
public interface AroundInterceptor  {

	void before(InterceptorContext ctx);

	void after(InterceptorContext ctx);
}
