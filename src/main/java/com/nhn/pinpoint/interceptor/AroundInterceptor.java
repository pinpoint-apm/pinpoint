package com.nhn.pinpoint.interceptor;

@Deprecated
public interface AroundInterceptor  {

	void before(InterceptorContext ctx);

	void after(InterceptorContext ctx);
}
