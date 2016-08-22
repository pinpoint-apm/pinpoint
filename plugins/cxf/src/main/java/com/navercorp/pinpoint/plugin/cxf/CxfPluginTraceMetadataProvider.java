package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class CxfPluginTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        context.addAnnotationKey(CxfPluginConstants.CXF_OPERATION);
        context.addAnnotationKey(CxfPluginConstants.CXF_ARGS);
    }

}
