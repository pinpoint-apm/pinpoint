package com.navercorp.pinpoint.profiler.modifier.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.MetaObject;


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
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
		}

		Trace trace = traceContext.currentTraceObject();

		if (trace == null) {
			return;
		}

		// 일단 이벤트가 세지 않는다는 가정하에 별도 timeout처리가 없음.
//		AsyncTrace asyncTrace = trace.createAsyncTrace();
//		asyncTrace.markBeforeTime();
//
//		asyncTrace.setAttachObject(new TimeObject());
//
//		setAsyncTrace.invoke(target, asyncTrace);
	}

    @Override
    public void setTraceContext(TraceContext traceContext) {

        this.traceContext = traceContext;
    }
}
