
package com.navercorp.pinpoint.plugin.db2;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;


public class DB2TypeProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(DB2PluginConstants.DB2, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
        context.addServiceType(DB2PluginConstants.DB2_EXECUTE_QUERY, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
        AnnotationKeyMatchers.exact(AnnotationKey.ARGS0);
    }

}
