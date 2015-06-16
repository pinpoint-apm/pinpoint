package com.navercorp.pinpoint.plugin.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetConstructor;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@TargetConstructor({"java.lang.String", "java.lang.String", "net.spy.memcached.ConnectionFactoryBuilder", "java.util.concurrent.CountDownLatch", "int", "int"})
public class CacheManagerConstructInterceptor implements SimpleAroundInterceptor, ArcusConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final MetadataAccessor serviceCodeAccessor;
    
    public CacheManagerConstructInterceptor(@Name(METADATA_SERVICE_CODE) MetadataAccessor serviceCodeAccessor) {
        this.serviceCodeAccessor = serviceCodeAccessor;
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

        serviceCodeAccessor.set(target, args[1]);
    }
}
