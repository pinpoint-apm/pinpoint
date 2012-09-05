package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private Method getUrl = null;

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }

		try {
			/**
			 * If method was not called by request handler, we skip tagging.
			 */
            String url = getUrl(target);
            Trace.recordRpcName("mysql", url);

			if (args.length > 0) {
				Trace.recordAttibute("Query", args[0]);
			}

			Trace.record(Annotation.ClientSend);

			StopWatch.start("ExecuteQueryMethodInterceptor");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private String getUrl(Object target) {
        try {
            // TODO classloading 시 해당 mehtod를 한번에 가져올수 없는지 검토.
            if(getUrl == null) {
                getUrl = target.getClass().getMethod("__getUrl");
            }
            return (String) getUrl.invoke(target);
        } catch (NoSuchMethodException e) {
            if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
        } catch (IllegalAccessException e) {
            if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
        } catch (InvocationTargetException e) {
            if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
        }
        return null;
    }

    @Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }

        if (Trace.getCurrentTraceId() == null) {
			return;
		}


		Trace.record(Annotation.ClientRecv, StopWatch.stopAndGetElapsed("ExecuteQueryMethodInterceptor"));
	}
}
