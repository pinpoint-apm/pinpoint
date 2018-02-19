package com.navercorp.pinpoint.plugin.akka.http;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author lopiter
 */
public class AkkaHttpMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(AkkaHttpConstants.AKKA_HTTP);
        context.addServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL);
    }

}
