package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.RootCallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;

public class WrappedCallStackFrameHolder {

    private WrappedRootCallStackFrame rootCallStackFrame;
    private WrappedCallStackFrame callStackFrame;

    public RootCallStackFrame get(final TraceContext traceContext, final Span span, final TraceId traceId, final boolean sampling) {
        if (this.rootCallStackFrame == null) {
            this.rootCallStackFrame = new WrappedRootCallStackFrame(traceContext);
        }
        rootCallStackFrame.setSpan(span);
        rootCallStackFrame.setTraceId(traceId);
        rootCallStackFrame.setSampling(sampling);
        return rootCallStackFrame;
    }

    public CallStackFrame get(final TraceContext traceContext, final SpanEvent spanEvent) {
        if (this.callStackFrame == null) {
            this.callStackFrame = new WrappedCallStackFrame(traceContext);
        }
        callStackFrame.setSpanEvent(spanEvent);

        return callStackFrame;
    }
}