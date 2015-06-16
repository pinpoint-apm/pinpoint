package com.navercorp.pinpoint.plugin.arcus.interceptor;

import net.sf.ehcache.Element;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetConstructor;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;

/**
 * @author harebox
 */
@TargetConstructor("net.sf.ehcache.Element")
public class FrontCacheGetFutureConstructInterceptor implements SimpleAroundInterceptor, ArcusConstants {

    // TODO This should be extracted from FrontCacheMemcachedClient.
    private static final String DEFAULT_FRONTCACHE_NAME = "front";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetadataAccessor cacheNameAccessor;
    private final MetadataAccessor cacheKeyAccessor;

    public FrontCacheGetFutureConstructInterceptor(@Name(MEATDATA_CACHE_NAME) MetadataAccessor cacheNameAccessor,
            @Name(ArcusConstants.METADATA_CACHE_KEY) MetadataAccessor cacheKeyAccessor) {
        this.cacheNameAccessor = cacheNameAccessor;
        this.cacheKeyAccessor = cacheKeyAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        // do nothing
        
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            cacheNameAccessor.set(target, DEFAULT_FRONTCACHE_NAME);
            
            if (args[0] instanceof Element) {
                Element element = (Element) args[0];
                cacheKeyAccessor.set(target, element.getObjectKey());
            }
        } catch (Exception e) {
            logger.error("failed to add metadata: {}", e);
        }
    }
}
