package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;

public class WrappedRecorderHolder {

    private WrappedSpanRecorder spanRecorder;
    private WrappedSpanEventRecorder spanEventRecorder;

    public SpanRecorder getSpanRecorder(final TraceContext traceContext, final Span span) {
        if (this.spanRecorder == null) {
            this.spanRecorder = new WrappedSpanRecorder(traceContext);
        }
        spanRecorder.setSpan(span);

        return spanRecorder;
    }

    public SpanEventRecorder getSpanEventRecorder(final TraceContext traceContext, final SpanEvent spanEvent) {
        if (this.spanEventRecorder == null) {
            this.spanEventRecorder = new WrappedSpanEventRecorder(traceContext);
        }
        spanEventRecorder.setSpanEvent(spanEvent);

        return spanEventRecorder;
    }
}