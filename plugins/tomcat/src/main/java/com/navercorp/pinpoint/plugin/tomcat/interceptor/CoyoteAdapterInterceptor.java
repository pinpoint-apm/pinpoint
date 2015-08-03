package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;


public class CoyoteAdapterInterceptor implements SimpleAroundInterceptor {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private InstrumentableMethod targetMethod;

    public CoyoteAdapterInterceptor(TraceContext context, InstrumentableMethod targetMethod) {
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
