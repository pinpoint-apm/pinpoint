package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;

public class RequestRecycleInterceptor implements SimpleAroundInterceptor, TomcatConstants {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MethodInfo targetMethod;
    private MetadataAccessor traceAccessor;
    private MetadataAccessor asyncAccessor;

    public RequestRecycleInterceptor(TraceContext context, @Cached MethodInfo targetMethod, @Name(METADATA_TRACE) MetadataAccessor traceAccessor, @Name(METADATA_ASYNC) MetadataAccessor asyncAccessor) {
        this.targetMethod = targetMethod;
        this.traceAccessor = traceAccessor;
        this.asyncAccessor = asyncAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        logger.beforeInterceptor(target, target.getClass().getName(), targetMethod.getName(), "", args);
        try {
            if (asyncAccessor.isApplicable(target)) {
                // reset
                asyncAccessor.set(target, Boolean.FALSE);
            }

            if (traceAccessor.isApplicable(target) && traceAccessor.get(target) != null) {
                Trace trace = traceAccessor.get(target);
                if (trace != null && trace.canSampled()) {
                    // end of root span
                    trace.markAfterTime();
                    trace.traceRootBlockEnd();
                }
                // reset
                traceAccessor.set(target, null);
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}