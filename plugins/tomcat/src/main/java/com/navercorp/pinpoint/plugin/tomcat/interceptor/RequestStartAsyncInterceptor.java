package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;

public class RequestStartAsyncInterceptor implements SimpleAroundInterceptor, TomcatConstants {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MethodInfo targetMethod;
    private MetadataAccessor asyncAccessor;

    public RequestStartAsyncInterceptor(TraceContext context, @Cached MethodInfo targetMethod, @Name(METADATA_ASYNC) MetadataAccessor asyncAccessor) {
        this.targetMethod = targetMethod;
        this.asyncAccessor = asyncAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        logger.beforeInterceptor(target, target.getClass().getName(), targetMethod.getName(), "", args);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        try {
            if (throwable == null) {
                asyncAccessor.set(target, Boolean.TRUE);
            }
        } catch (Throwable t) {
            logger.warn("Failed to after process. {}", t.getMessage(), t);
        }

    }
}
