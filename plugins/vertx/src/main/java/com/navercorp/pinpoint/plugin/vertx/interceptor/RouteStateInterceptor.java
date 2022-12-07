package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

public class RouteStateInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final TraceContext context;

    public RouteStateInterceptor(TraceContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final RoutingContext routingContext = ArrayArgumentUtils.getArgument(args, 0, RoutingContext.class);
        if (routingContext != null) {
            Route route = routingContext.currentRoute();
            if (route != null) {
                String path = route.getPath();
                logger.debug("vertx uriTemplate:{}", path);

                Trace trace = context.currentRawTraceObject();
                if (trace != null) {
                    trace.recordUriTemplate(path);
                }
            }

        }
    }
}
