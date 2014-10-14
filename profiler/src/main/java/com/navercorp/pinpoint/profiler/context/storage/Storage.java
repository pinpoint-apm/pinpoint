package com.nhn.pinpoint.profiler.context.storage;

import com.nhn.pinpoint.profiler.context.Span;
import com.nhn.pinpoint.profiler.context.SpanEvent;

/**
 * @author emeroad
 */
public interface Storage {

    /**
     *
     * @param spanEvent
     */
    void store(SpanEvent spanEvent);

    /**
     *
     * @param span
     */
    void store(Span span);
}
