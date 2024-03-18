package com.navercorp.pinpoint.plugin.spring.web.javax.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class ExposePathWithinMappingInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final TraceContext traceContext;
    private final Boolean uriStatCollectMethod;

    public ExposePathWithinMappingInterceptor(final TraceContext traceContext, Boolean uriStatCollectMethod) {
        this.traceContext = traceContext;
        this.uriStatCollectMethod = uriStatCollectMethod;
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
            final SpanRecorder spanRecorder = trace.getSpanRecorder();
            final String url = ArrayArgumentUtils.getArgument(args, 0, String.class);

            if (isDebug) {
                logger.debug("Attempt recording URI with template: {}", url);
            }

            if (StringUtils.hasLength(url)) {
                spanRecorder.recordUriTemplate(url);
            }

            if (uriStatCollectMethod) {
                final HttpServletRequest request = ArrayArgumentUtils.getArgument(args, 2, HttpServletRequest.class);
                final String method = request.getMethod();
                if (StringUtils.hasLength(method)) {
                    spanRecorder.recordUriHttpMethod(method);
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }
}
