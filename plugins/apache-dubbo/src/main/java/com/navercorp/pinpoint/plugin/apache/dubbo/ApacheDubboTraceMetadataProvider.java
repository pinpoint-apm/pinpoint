package com.navercorp.pinpoint.plugin.apache.dubbo;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author K
 * @date 2019-06-14-14:00
 */
public class ApacheDubboTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
        context.addServiceType(ApacheDubboConstants.DUBBO_CONSUMER_SERVICE_TYPE);
        context.addServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        context.addAnnotationKey(ApacheDubboConstants.DUBBO_ARGS_ANNOTATION_KEY);
        context.addAnnotationKey(ApacheDubboConstants.DUBBO_RESULT_ANNOTATION_KEY);
        context.addAnnotationKey(ApacheDubboConstants.DUBBO_RPC_ANNOTATION_KEY);
    }
}
