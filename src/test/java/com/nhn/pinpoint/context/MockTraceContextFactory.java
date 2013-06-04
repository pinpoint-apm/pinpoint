package com.nhn.pinpoint.context;

import com.nhn.pinpoint.profiler.context.BypassStorageFactory;
import com.nhn.pinpoint.profiler.context.DefaultTraceContext;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.sender.LoggingDataSender;

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
