package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * protected int executeUpdate(String sql, boolean isBatch, boolean
 * returnGeneratedKeys)
 * 
 * @author netspider
 * 
 */
public class ExecuteUpdateMethodInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(ExecuteUpdateMethodInterceptor.class.getName());

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		System.out.println("ExecuteUpdateMethodInterceptor.before");

		/**
		 * If method was not called by request handler, we skip tagging.
		 */
		if (Trace.getCurrentTraceId() == null) {
			return;
		}

        Trace.traceBlockBegin();
        try {
			Trace.recordRpcName("mysql", "");

			// TODO: add destination address
			if (args.length > 0) {
				Trace.recordAttibute("Query", args[0]);
			}

			Trace.record(Annotation.ClientSend);

			StopWatch.start("ExecuteUpdateMethodInterceptor");
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		} finally {
			Trace.traceBlockEnd();
		}
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		System.out.println("ExecuteUpdateMethodInterceptor.after");
		if (Trace.getCurrentTraceId() == null) {
			return;
		}
		
		Trace.traceBlockBegin();
		Trace.record(Annotation.ClientRecv, StopWatch.stopAndGetElapsed("ExecuteUpdateMethodInterceptor"));
		Trace.traceBlockEnd();
	}
}
