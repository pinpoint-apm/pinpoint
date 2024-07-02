package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.Set;

public class RouteStateInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private final TraceContext context;
    private final Boolean uriStatUseUserInput;
    private final Boolean uriStatCollectMethod;

    public RouteStateInterceptor(TraceContext context, Boolean uriStatUseUserInput, Boolean uriStatCollectMethod) {
        this.context = Objects.requireNonNull(context, "context");
        this.uriStatUseUserInput = uriStatUseUserInput;
        this.uriStatCollectMethod = uriStatCollectMethod;
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
                    final SpanRecorder spanRecorder = trace.getSpanRecorder();
                    final String path = route.getPath();
                    if (StringUtils.hasLength(path)) {
                        spanRecorder.recordUriTemplate(path, false);
                    }

                    if (uriStatCollectMethod) {
                        final Set<?> methods = route.methods();
                        if (CollectionUtils.hasLength(methods)) {
                            spanRecorder.recordUriHttpMethod(methods.toString());
                        }
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
