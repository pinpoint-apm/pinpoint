package com.navercorp.pinpoint.plugin.jdbc.sqlite;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class SqlitePluginTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(SqlitePluginConstants.SQLITE, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
        context.addServiceType(SqlitePluginConstants.SQLITE_EXECUTE_QUERY, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
    }

}
