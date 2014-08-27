package com.nhn.pinpoint.profiler.context.storage;

import com.nhn.pinpoint.profiler.context.Span;
import com.nhn.pinpoint.profiler.context.SpanEvent;
import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 * @author emeroad
 */
public interface Storage {

    /**
     * store(SpanEvent spanEvent)와 store(Span span)간 동기화가 구현되어 있어야 한다.
     *
     * @param spanEvent
     */
    void store(SpanEvent spanEvent);

    /**
     * store(SpanEvent spanEvent)와 store(Span span)간 동기화가 구현되어 있어야 한다.
     *
     * @param span
     */
    void store(Span span);
}
