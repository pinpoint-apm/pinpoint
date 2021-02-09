package com.navercorp.pinpoint.threadx;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class ThreadPoolTraceMetadataProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(ThreadPoolConstants.THREAD_POOL_EXECUTOR);
    }
}
