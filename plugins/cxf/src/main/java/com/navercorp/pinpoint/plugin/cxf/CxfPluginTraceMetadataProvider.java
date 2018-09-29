package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author barney
 * @author Victor.Zxy
 */
public class CxfPluginTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(CxfPluginConstants.CXF_SERVER_SERVICE_TYPE);
        context.addServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        context.addAnnotationKey(CxfPluginConstants.CXF_OPERATION);
        context.addAnnotationKey(CxfPluginConstants.CXF_ARGS);
        context.addAnnotationKey(CxfPluginConstants.CXF_URI);
        context.addAnnotationKey(CxfPluginConstants.CXF_METHOD);
        context.addAnnotationKey(CxfPluginConstants.CXF_TYPE);
    }

}
