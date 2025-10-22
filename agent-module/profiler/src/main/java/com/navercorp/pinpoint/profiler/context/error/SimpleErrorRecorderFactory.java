package com.navercorp.pinpoint.profiler.context.error;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;

public class SimpleErrorRecorderFactory implements ErrorRecorderFactory {
    @Inject
    public SimpleErrorRecorderFactory() {
    }

    @Override
    public ErrorRecorder newRecorder(LocalTraceRoot traceRoot) {
        return new SimpleErrorRecorder(traceRoot);
    }
}
