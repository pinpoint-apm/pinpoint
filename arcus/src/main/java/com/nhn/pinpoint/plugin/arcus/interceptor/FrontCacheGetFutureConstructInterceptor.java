package com.nhn.pinpoint.plugin.arcus.interceptor;

import net.sf.ehcache.Element;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAfterInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.plugin.arcus.accessor.CacheKeyAccessor;
import com.nhn.pinpoint.plugin.arcus.accessor.CacheNameAccessor;

/**
 * @author harebox
 */
public class FrontCacheGetFutureConstructInterceptor implements SimpleAfterInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // TODO This should be extracted from FrontCacheMemcachedClient.
    private static final String DEFAULT_FRONTCACHE_NAME = "front";

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            ((CacheNameAccessor)target).__setCacheName(DEFAULT_FRONTCACHE_NAME);
            
            if (args[0] instanceof Element) {
                Element element = (Element) args[0];
                ((CacheKeyAccessor)target).__setCacheKey((String)element.getObjectKey());
            }
        } catch (Exception e) {
            logger.error("failed to add metadata: {}", e);
        }
    }
}
