package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;

public class DefaultAsyncContextTest extends AsyncContextTest {
    @Override
    AsyncContext newAsyncContext(boolean canSampled) {
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();
        return new DefaultAsyncContext(asyncTraceContext, traceRoot, asyncId, 0, canSampled);
    }
}
