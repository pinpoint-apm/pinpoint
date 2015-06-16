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
@TargetMethod(name="addOp", paramTypes={"java.lang.String", "net.spy.memcached.ops.Operation"})
public class AddOpInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final MetadataAccessor serviceCodeAccessor;
    
    public AddOpInterceptor(@Name(ArcusConstants.METADATA_SERVICE_CODE) MetadataAccessor serviceCodeAccessor) {
        this.serviceCodeAccessor = serviceCodeAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        String serviceCode = serviceCodeAccessor.get(target);
        serviceCodeAccessor.set(args[1], serviceCode);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }
}
