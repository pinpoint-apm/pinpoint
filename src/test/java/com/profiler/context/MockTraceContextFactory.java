package com.profiler.context;

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
