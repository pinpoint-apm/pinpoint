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
				Trace.recordAttibute("Query", args[0]);
			}

			Trace.record(Annotation.ClientSend);

			StopWatch.start("ExecuteQueryMethodInterceptor");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		if (Trace.getCurrentTraceId() == null) {
			return;
		}

		Trace.record(Annotation.ClientRecv, StopWatch.stopAndGetElapsed("ExecuteQueryMethodInterceptor"));
	}
}
