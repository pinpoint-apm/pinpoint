package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import net.sf.ehcache.Element;

/**
 * @author harebox
 */
public class FrontCacheGetFutureConstructInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // TODO This should be extracted from FrontCacheMemcachedClient.
    private static final String DEFAULT_FRONTCACHE_NAME = "front";

    private MetaObject<Object> setCacheName = new MetaObject<Object>("__setCacheName", String.class);
    private MetaObject<Object> setCacheKey = new MetaObject<Object>("__setCacheKey", String.class);

    @Override
    public void before(Object target, Object[] args) {
        // do nothing.
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            setCacheName.invoke(target, DEFAULT_FRONTCACHE_NAME);

            if (args[0] instanceof Element) {
                Element element = (Element) args[0];
                setCacheKey.invoke(target, element.getObjectKey());
            }
        } catch (Exception e) {
            logger.error("failed to add metadata: {}", e);
        }
    }
}
