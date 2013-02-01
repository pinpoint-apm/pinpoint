package com.profiler.modifier.arcus.interceptors;

import com.profiler.context.*;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 *
 */
public class BaseOperationConstructInterceptor implements StaticAfterInterceptor {

	private final Logger logger = Logger.getLogger(BaseOperationConstructInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

	private MetaObject<Object> setAsyncTrace = new MetaObject<Object>("__setAsyncTrace", Object.class);

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
		}
		
		TraceContext traceContext = TraceContext.getTraceContext();
		Trace trace = traceContext.currentTraceObject();
		
		if (trace == null) {
			return;
		}
		
		// 일단 이벤트가 세지 않는다는 가정하에 별도 timeout처리가 없음.
		AsyncTrace asyncTrace = trace.createAsyncTrace();
		asyncTrace.markBeforeTime();

		asyncTrace.setAttachObject(new TimeObject());

		setAsyncTrace.invoke(target, asyncTrace);
	}
}
