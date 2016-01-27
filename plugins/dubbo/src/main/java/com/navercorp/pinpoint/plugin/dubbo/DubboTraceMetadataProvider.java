package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author Jinkai.Ma
 */
public class DubboTraceMetadataProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(DubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
        context.addServiceType(DubboConstants.DUBBO_CONSUMER_SERVICE_TYPE);
        context.addAnnotationKey(DubboConstants.DUBBO_ARGS_ANNOTATION_KEY);
        context.addAnnotationKey(DubboConstants.DUBBO_RESULT_ANNOTATION_KEY);
    }
}
