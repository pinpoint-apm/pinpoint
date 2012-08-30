package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;

/**
 * protected int executeUpdate(String sql, boolean isBatch, boolean
 * returnGeneratedKeys)
 * 
 * @author netspider
 * 
 */
public class ExecuteUpdateMethodInterceptor implements StaticAroundInterceptor {

	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		System.out.println("ExecuteUpdateMethodInterceptor.before");
		try {
			/**
			 * If method was not called by request handler, we skip tagging.
			 */
			if (Trace.getCurrentTraceId() == null) {
				return;
			}

			Trace.recordRpcName("mysql", "");

			//
			// TODO: add destination address
			//

			if (args.length > 0) {
				Trace.record("Query=" + args[0]);
			}

			Trace.record(new Annotation.ClientSend());

			StopWatch.start("ExecuteUpdateMethodInterceptor");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		System.out.println("ExecuteUpdateMethodInterceptor.after");
		if (Trace.getCurrentTraceId() == null) {
			return;
		}

		Trace.record(new Annotation.ClientRecv(), StopWatch.stopAndGetElapsed("ExecuteUpdateMethodInterceptor"));
	}
}
