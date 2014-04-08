package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

import com.nhn.pinpoint.bootstrap.context.AsyncTrace;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import com.nhn.pinpoint.bootstrap.util.TimeObject;

/**
 * @author emeroad
 */
@Deprecated
public class BaseOperationConstructInterceptor implements SimpleAroundInterceptor, TraceContextSupport {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<Object> setAsyncTrace = new MetaObject<Object>("__setAsyncTrace", Object.class);
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
	public void after(Object target, Object[] args, Object result) {
		if (isDebug) {
            logger.afterInterceptor(target, args, result);
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
