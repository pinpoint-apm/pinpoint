package com.navercorp.pinpoint.profiler.interceptor;

/**
 * @author emeroad
 */
@Deprecated
public interface AroundInterceptor  {

	void before(InterceptorContext ctx);

	void after(InterceptorContext ctx);
}
