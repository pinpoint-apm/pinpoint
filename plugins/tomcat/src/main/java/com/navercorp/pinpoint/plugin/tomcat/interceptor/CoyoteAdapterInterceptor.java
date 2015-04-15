package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.NoCache;

public class CoyoteAdapterInterceptor implements SimpleAroundInterceptor {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MethodInfo targetMethod;

    public CoyoteAdapterInterceptor(TraceContext context, MethodInfo targetMethod) {
        this.targetMethod = targetMethod;
    }

    @Override
    public void before(Object target, Object[] args) {
        logger.beforeInterceptor(target, target.getClass().getName(), targetMethod.getName(), "", args);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, target.getClass().getName(), targetMethod.getName(), "", args, result, throwable);
    }

}
