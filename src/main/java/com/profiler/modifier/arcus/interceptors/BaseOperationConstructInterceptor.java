package com.profiler.modifier.arcus.interceptors;

import com.profiler.logging.Logger;

import com.profiler.context.AsyncTrace;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.TimeObject;

/**
 *
 */
public class BaseOperationConstructInterceptor implements StaticAfterInterceptor, TraceContextSupport {

	private final Logger logger = LoggerFactory.getLogger(BaseOperationConstructInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<Object> setAsyncTrace = new MetaObject<Object>("__setAsyncTrace", Object.class);
    private TraceContext traceContext;

    @Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
		}
		
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

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}
