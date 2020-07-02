package com.navercorp.pinpoint.plugin.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetricRegistry;

public class PlatformDependentInterceptor implements AroundInterceptor {

	private CustomMetricRegistry customMetricMonitorRegistry;

	public PlatformDependentInterceptor(CustomMetricRegistry customMetricMonitorRegistry) {
		this.customMetricMonitorRegistry = customMetricMonitorRegistry;
	}

	@Override
	public void before(Object target, Object[] args) {

	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {

	}
}
