package com.nhn.pinpoint.bootstrap.interceptor;

/**
 * @author emeroad
 */
@Deprecated
public interface AroundInterceptor  {

	void before(InterceptorContext ctx);

	void after(InterceptorContext ctx);
}
