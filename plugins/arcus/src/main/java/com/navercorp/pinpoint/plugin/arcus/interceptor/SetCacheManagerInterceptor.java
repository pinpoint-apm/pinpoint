package com.navercorp.pinpoint.plugin.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetMethod;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@TargetMethod(name="setCacheManager", paramTypes="net.spy.memcached.CacheManager")
public class SetCacheManagerInterceptor implements SimpleAroundInterceptor, ArcusConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final MetadataAccessor serviceCodeAccessor;
    
    public SetCacheManagerInterceptor(@Name(METADATA_SERVICE_CODE) MetadataAccessor serviceCodeAccessor) {
        this.serviceCodeAccessor = serviceCodeAccessor;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // do nothing
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        String serviceCode = serviceCodeAccessor.get(args[0]);
        serviceCodeAccessor.set(target, serviceCode);
    }
}
