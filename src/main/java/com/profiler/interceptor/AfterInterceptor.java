package com.profiler.interceptor;

public interface AfterInterceptor extends Interceptor {
	void after(InterceptorContext ctx);
}
