package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.AsyncStateSupport;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;

import java.util.Objects;

public class StatefulDisableAsyncContext extends DisableAsyncContext implements AsyncStateSupport {

    private final AsyncState asyncState;

    public StatefulDisableAsyncContext(AsyncTraceContext asyncTraceContext, Binder<Trace> binder, LocalTraceRoot traceRoot, AsyncState asyncState) {
        super(asyncTraceContext, binder, traceRoot);
        this.asyncState = Objects.requireNonNull(asyncState, "asyncState");

    }

    @Override
    public AsyncState getAsyncState() {
        return asyncState;
    }

    @Override
    public void finish() {
        this.asyncState.finish();
    }

    @Override
    public String toString() {
        return "StatefulDisableAsyncContext{" +
                "asyncState=" + asyncState +
                "} " + super.toString();
    }
}
