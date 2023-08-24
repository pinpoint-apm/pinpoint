package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpanRecorder {
    final Recorder<SpanType> recorder;

    public SpanRecorder(Recorder<SpanType> recorder) {
        this.recorder = Objects.requireNonNull(recorder, "recorder");
    }

    public List<SpanEvent> getCurrentSpanEvents() {
        List<SpanEvent> spanEvents = new ArrayList<>();
        for (SpanType value : this.recorder) {
            if (value instanceof SpanChunk) {
                final SpanChunk spanChunk = (SpanChunk) value;
                spanEvents.addAll(spanChunk.getSpanEventList());
            }
        }
        return spanEvents;
    }

    public List<Span> getCurrentSpans() {
        List<Span> rootSpans = new ArrayList<>();
        for (Object value : this.recorder) {
            if (value instanceof Span) {
                Span span = (Span) value;
                rootSpans.add(span);
            }
        }
        return rootSpans;
    }
}
