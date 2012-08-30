package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;

/**
 * 
 * @author netspider
 * 
 */
public class ExecuteQueryMethodInterceptor implements StaticAroundInterceptor {

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		try {
			Trace.recordRpcName("mysql", "");

			if (args.length > 0) {
				Trace.record("Query=" + args[0]);
			}

			Trace.record(new Annotation.ClientSend());

			StopWatch.start("ExecuteQueryMethodInterceptor");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		Trace.record(new Annotation.ClientRecv(), StopWatch.stopAndGetElapsed("ExecuteQueryMethodInterceptor"));
	}
}
