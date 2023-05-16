package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;


public class ExposePathWithinMappingInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final TraceContext traceContext;

    public ExposePathWithinMappingInterceptor(final TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final String url = ArrayArgumentUtils.getArgument(args, 0, String.class);
            if (StringUtils.hasLength(url)) {
                final SpanRecorder spanRecorder = trace.getSpanRecorder();
                spanRecorder.recordUriTemplate(url);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }
}
