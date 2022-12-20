package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

public class AbstractUrlHandlerMappingInterceptor implements AroundInterceptor {
    private final TraceContext traceContext;

    public AbstractUrlHandlerMappingInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace != null) {
            ServerWebExchange webExchange = ArrayArgumentUtils.getArgument(args, 1, ServerWebExchange.class);
            String uri = extractAttribute(webExchange, SpringWebFluxConstants.SPRING_WEBFLUX_DEFAULT_URI_ATTRIBUTE_KEYS);
            if (uri != null) {
                SpanRecorder spanRecorder = trace.getSpanRecorder();
                spanRecorder.recordUriTemplate(uri, false);
            }
        }
    }

    private String extractAttribute(ServerWebExchange webExchange, String[] keys) {
        for (String attributeName : keys) {
            Object uriMapping = webExchange.getAttribute(attributeName);
            if (!(uriMapping instanceof PathPattern)) {
                continue;
            }

            String uriTemplate = ((PathPattern) uriMapping).getPatternString();
            if (StringUtils.hasLength(uriTemplate)) {
                return uriTemplate;
            }
        }
        return null;
    }

}
