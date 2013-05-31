package com.nhn.pinpoint.context;

import com.profiler.context.BypassStorageFactory;
import com.profiler.context.DefaultTraceContext;
import com.profiler.sender.LoggingDataSender;

/**
 *
 */
public class MockTraceContextFactory {
    public TraceContext create() {
        DefaultTraceContext traceContext = new DefaultTraceContext();
        BypassStorageFactory bypassStorageFactory = new BypassStorageFactory(new LoggingDataSender());
        traceContext.setStorageFactory(bypassStorageFactory);
        return traceContext;
    }
}
