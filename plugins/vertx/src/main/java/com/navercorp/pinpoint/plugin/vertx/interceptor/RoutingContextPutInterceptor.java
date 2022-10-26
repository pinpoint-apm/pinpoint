package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

public class RoutingContextPutInterceptor implements AroundInterceptor {
    private final InterceptorScope scope;

    public RoutingContextPutInterceptor(final InterceptorScope scope) {
        this.scope = Objects.requireNonNull(scope);
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        RoutingContext context = (RoutingContext) target;
        Object value = context.data().get(VertxConstants.VERTX_URL_STAT_TEMPLATE_KEY);
        if (!Objects.isNull(value)) {
            VertxUrlTemplate attachment = (VertxUrlTemplate)scope.getCurrentInvocation().getAttachment();
            attachment.setUrlTemplate((String)value);
        }
    }
}
