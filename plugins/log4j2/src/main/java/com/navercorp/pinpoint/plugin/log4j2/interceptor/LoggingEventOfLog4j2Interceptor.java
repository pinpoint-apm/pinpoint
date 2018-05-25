package com.navercorp.pinpoint.plugin.log4j2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import org.apache.logging.log4j.ThreadContext;

/**
 * @Author: guoyankui
 * @DATE: 2018/5/25 11:53 AM
 */
public class LoggingEventOfLog4j2Interceptor implements AroundInterceptor0 {

    private static final String TRANSACTION_ID = "PtxId";
    private static final String SPAN_ID = "PspanId";

    private final TraceContext traceContext;

    public LoggingEventOfLog4j2Interceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target) {
        Trace trace = traceContext.currentTraceObject();

        if (trace == null) {
            ThreadContext.remove(TRANSACTION_ID);
            ThreadContext.remove(SPAN_ID);
            return;
        } else {
            ThreadContext.put(TRANSACTION_ID, trace.getTraceId().getTransactionId());
            ThreadContext.put(SPAN_ID, String.valueOf(trace.getTraceId().getSpanId()));
        }
    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }
}
