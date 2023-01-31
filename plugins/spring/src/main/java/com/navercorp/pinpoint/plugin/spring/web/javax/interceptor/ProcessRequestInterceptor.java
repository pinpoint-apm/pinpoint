package com.navercorp.pinpoint.plugin.spring.web.javax.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;

import javax.servlet.ServletRequest;

public class ProcessRequestInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final TraceContext traceContext;
    private final Boolean uriStatUseUserInput;

    public ProcessRequestInterceptor(final TraceContext traceContext, Boolean uriStatUseUserInput) {
        this.traceContext = traceContext;
        this.uriStatUseUserInput = uriStatUseUserInput;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (Boolean.FALSE == uriStatUseUserInput) {
            return;
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final ServletRequest request = ArrayArgumentUtils.getArgument(args, 0, ServletRequest.class);
            if (request != null) {
                final String uri = ServletRequestAttributeUtils.extractAttribute(request, SpringWebMvcConstants.SPRING_MVC_URI_USER_INPUT_ATTRIBUTE_KEYS);
                if (StringUtils.hasLength(uri)) {
                    final SpanRecorder spanRecorder = trace.getSpanRecorder();
                    spanRecorder.recordUriTemplate(uri, true);
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }
}
