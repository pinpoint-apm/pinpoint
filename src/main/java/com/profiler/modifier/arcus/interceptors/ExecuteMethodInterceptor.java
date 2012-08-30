package com.profiler.modifier.arcus.interceptors;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;

public class ExecuteMethodInterceptor implements StaticAroundInterceptor {

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		System.out.println("\n\n\n\nARCUS BEFORE");

		StopWatch.start("ExecuteMethodInterceptor");
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		System.out.println("\n\n\n\nARCUS AFTER");
		Trace.record(new Annotation.ClientRecv(), StopWatch.stopAndGetElapsed("ExecuteMethodInterceptor"));
	}
}