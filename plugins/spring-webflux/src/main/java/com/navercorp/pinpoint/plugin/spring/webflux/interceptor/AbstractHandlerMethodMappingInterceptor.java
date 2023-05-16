package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;
import org.springframework.web.server.ServerWebExchange;

public class AbstractHandlerMethodMappingInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final TraceContext traceContext;

    public AbstractHandlerMethodMappingInterceptor(final TraceContext traceContext) {
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
            final ServerWebExchange webExchange = ArrayArgumentUtils.getArgument(args, 0, ServerWebExchange.class);
            if (webExchange != null) {
                final String uri = ServerWebExchangeAttributeUtils.extractAttribute(webExchange, SpringWebFluxConstants.SPRING_WEBFLUX_DEFAULT_URI_ATTRIBUTE_KEYS);
                if (StringUtils.hasLength(uri)) {
                    final SpanRecorder spanRecorder = trace.getSpanRecorder();
                    spanRecorder.recordUriTemplate(uri, false);
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }
}
