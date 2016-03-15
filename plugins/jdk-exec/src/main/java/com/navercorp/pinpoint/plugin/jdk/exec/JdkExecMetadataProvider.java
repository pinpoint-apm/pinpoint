package com.navercorp.pinpoint.plugin.jdk.exec;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author lisn
 */
public class JdkExecMetadataProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(JdkExecConstants.SERVICE_TYPE);
    }
}
