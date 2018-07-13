package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class OpenwhiskMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
        context.addServiceType(OpenwhiskConstants.OPENWHISK_CONTROLLER);
        context.addServiceType(OpenwhiskConstants.OPENWHISK_INVOKER);

        context.addServiceType(OpenwhiskConstants.COUCHDB, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
        context.addServiceType(OpenwhiskConstants.COUCHDB_EXECUTE_QUERY, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));

        context.addAnnotationKey(OpenwhiskConstants.MARKER_MESSAGE);
    }

}
