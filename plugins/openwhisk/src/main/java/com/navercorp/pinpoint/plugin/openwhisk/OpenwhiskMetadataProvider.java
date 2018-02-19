package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author lopiter
 */
public class OpenwhiskMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
        context.addServiceType(OpenwhiskConstants.OPENWHISK_INVOKER);
    }

}
