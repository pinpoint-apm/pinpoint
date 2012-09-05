package com.profiler.interceptor;

public interface StaticAfterInterceptor extends Interceptor {
	void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result);
}
