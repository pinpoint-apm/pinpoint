package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

public class RouteStateInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final TraceContext context;
    private final Boolean uriStatUseUserInput;

    public RouteStateInterceptor(TraceContext context, Boolean uriStatUseUserInput) {
        this.context = Objects.requireNonNull(context, "context");
        this.uriStatUseUserInput = uriStatUseUserInput;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final Trace trace = context.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final RoutingContext routingContext = ArrayArgumentUtils.getArgument(args, 0, RoutingContext.class);
            if (routingContext != null) {
                if (uriStatUseUserInput) {
                    if (recordWithUserInput(routingContext, trace)) {
                        return;
                    }
                }
                final Route route = routingContext.currentRoute();
                if (route != null) {
                    final String path = route.getPath();
                    if (StringUtils.hasLength(path)) {
                        final SpanRecorder spanRecorder = trace.getSpanRecorder();
                        spanRecorder.recordUriTemplate(path, false);
                    }
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private boolean recordWithUserInput(RoutingContext routingContext, Trace trace) {
        for (String key : VertxConstants.VERTX_URI_MAPPING_CONTEXT_KEYS) {
            final Object value = routingContext.get(key);
            if (!(value instanceof String)) {
                continue;
            }
            final String userUriTemplate = (String) value;
            if (StringUtils.hasLength(userUriTemplate)) {
                final SpanRecorder spanRecorder = trace.getSpanRecorder();
                spanRecorder.recordUriTemplate(userUriTemplate, true);
                return true;
            }
        }
        return false;
    }
}
