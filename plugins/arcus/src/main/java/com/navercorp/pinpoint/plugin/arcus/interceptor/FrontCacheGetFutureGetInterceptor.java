package com.navercorp.pinpoint.plugin.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;

/**
 * @author harebox
 */
@Group(ArcusConstants.ARCUS_SCOPE)
public class FrontCacheGetFutureGetInterceptor implements SimpleAroundInterceptor, ArcusConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;
    private final MetadataAccessor cacheNameAccessor;
    
    public FrontCacheGetFutureGetInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, @Name(MEATDATA_CACHE_NAME) MetadataAccessor cacheNameAccessor) {
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
        this.cacheNameAccessor = cacheNameAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            trace.recordApi(methodDescriptor);

//            String cacheKey = (String) getCacheKey.invoke(target);
//            if (cacheKey != null) {
//                // annotate it.
//            }

            String cacheName = cacheNameAccessor.get(target);
            if (cacheName != null) {
                trace.recordDestinationId(cacheName);
            }

            trace.recordServiceType(ARCUS_EHCACHE_FUTURE_GET);
            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}
