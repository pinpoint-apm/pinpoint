package com.nhn.pinpoint.plugin.arcus.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.plugin.arcus.accessor.CacheNameAccessor;

/**
 * @author harebox
 */
public class FrontCacheGetFutureGetInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;
    
    public FrontCacheGetFutureGetInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
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

            String cacheName = ((CacheNameAccessor)target).__getCacheName();
            if (cacheName != null) {
                trace.recordDestinationId(cacheName);
            }

            trace.recordServiceType(ServiceType.ARCUS_EHCACHE_FUTURE_GET);
            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}
