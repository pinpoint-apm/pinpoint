package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;

import javax.servlet.ServletRequest;

public class ProcessRequestInterceptor implements AroundInterceptor {

    private TraceContext traceContext;
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
        Trace trace = traceContext.currentRawTraceObject();
        ServletRequest request = ArrayArgumentUtils.getArgument(args, 0, ServletRequest.class);

        if (uriStatUseUserInput && (trace != null)) {
            String uri = extractAttribute(request, SpringWebMvcConstants.SPRING_MVC_URI_USER_INPUT_ATTRIBUTE_KEYS);
            if (uri != null) {
                SpanRecorder spanRecorder = trace.getSpanRecorder();
                spanRecorder.recordUriTemplate(uri, true);
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
