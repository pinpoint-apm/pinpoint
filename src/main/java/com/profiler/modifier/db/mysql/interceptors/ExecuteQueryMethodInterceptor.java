package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author netspider
 * 
 */
public class ExecuteQueryMethodInterceptor implements StaticAroundInterceptor {

	private final Logger logger = Logger.getLogger(ExecuteQueryMethodInterceptor.class.getName());

	private final MetaObject<String> getUrl = new MetaObject("", "__getUrl");

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
		}

		if (Trace.getCurrentTraceId() == null) {
			return;
		}

		Trace.traceBlockBegin();

		try {
			/**
			 * If method was not called by request handler, we skip tagging.
			 */
			String url = (String) this.getUrl.invoke(target);
			Trace.recordRpcName("mysql", url);

			if (args.length > 0) {
				Trace.recordAttibute("Query", args[0]);
			}

			Trace.record(Annotation.ClientSend);

			StopWatch.start("ExecuteQueryMethodInterceptor");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Trace.traceBlockEnd();
		}
	}


    @Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
		}

		if (Trace.getCurrentTraceId() == null) {
			return;
		}
		
		Trace.traceBlockBegin();
        Trace.recordAttibute("Success", InterceptorUtils.isSuccess(result));
		Trace.record(Annotation.ClientRecv, StopWatch.stopAndGetElapsed("ExecuteQueryMethodInterceptor"));
		Trace.traceBlockEnd();
	}
}
