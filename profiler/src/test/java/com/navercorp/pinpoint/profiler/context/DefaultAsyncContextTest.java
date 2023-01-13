package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;

public class DefaultAsyncContextTest extends AsyncContextTest {
    @Override
    AsyncContext newAsyncContext(boolean canSampled) {
        Binder<Trace> binder = new ThreadLocalBinder<>();
        AsyncTraceContext asyncTraceContext = newAsyncTraceContext();
        if (canSampled) {
            return new DefaultAsyncContext(asyncTraceContext, binder, traceRoot, asyncId, 0);
        } else {
            return new DisableAsyncContext(asyncTraceContext, binder, traceRoot);
        }
    }
}
