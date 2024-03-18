package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;

public class DefaultAsyncContextTest extends AsyncContextTest {
    @Override
    AsyncContext newAsyncContext(boolean canSampled) {
        Binder<Trace> binder = new ThreadLocalBinder<>();
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();
        if (canSampled) {
            return AsyncContexts.remote(asyncTraceContext, binder, 0).sync(traceRoot, asyncId);
        } else {
            return AsyncContexts.local(asyncTraceContext, binder).sync(traceRoot);
        }
    }
}
