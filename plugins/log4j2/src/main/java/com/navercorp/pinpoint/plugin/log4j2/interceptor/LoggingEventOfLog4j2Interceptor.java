package com.navercorp.pinpoint.plugin.log4j2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import org.slf4j.MDC;

/**
 * @Author: https://github.com/licoco/pinpoint
 * @Date: 2019/1/4 10:53
 * @Version: 1.0
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
            MDC.remove(TRANSACTION_ID);
            MDC.remove(SPAN_ID);
            return;
        } else {
            MDC.put(TRANSACTION_ID, trace.getTraceId().getTransactionId());
            MDC.put(SPAN_ID, String.valueOf(trace.getTraceId().getSpanId()));
        }
    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }
}
