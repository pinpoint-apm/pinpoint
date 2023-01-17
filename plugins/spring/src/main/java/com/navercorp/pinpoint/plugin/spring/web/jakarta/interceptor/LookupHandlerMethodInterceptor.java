package com.navercorp.pinpoint.plugin.spring.web.jakarta.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;
import jakarta.servlet.ServletRequest;

public class LookupHandlerMethodInterceptor implements AroundInterceptor {
    private final TraceContext traceContext;

    public LookupHandlerMethodInterceptor(final TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace != null) {
            ServletRequest request = ArrayArgumentUtils.getArgument(args, 1, ServletRequest.class);
            String uri = extractAttribute(request, SpringWebMvcConstants.SPRING_MVC_DEFAULT_URI_ATTRIBUTE_KEYS);
            if (uri != null) {
                SpanRecorder spanRecorder = trace.getSpanRecorder();
                spanRecorder.recordUriTemplate(uri, false);
            }
        }
    }

    private String extractAttribute(ServletRequest request, String[] keys) {
        for (String attributeName : keys) {
            Object uriMapping = request.getAttribute(attributeName);
            if (!(uriMapping instanceof String)) {
                continue;
            }

            if (StringUtils.hasLength((String) uriMapping)) {
                return (String) uriMapping;
            }
        }
        return null;
    }
}
