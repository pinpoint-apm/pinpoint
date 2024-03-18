package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * 
 * @author huangpengjie@fang.com
 * 
 */
public class ResinTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(ResinConstants.RESIN);
        context.addServiceType(ResinConstants.RESIN_METHOD);
    }

}
