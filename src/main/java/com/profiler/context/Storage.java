package com.profiler.context;

import com.profiler.sender.DataSender;

/**
 *
 */
public interface Storage {

    void setDataSender(DataSender dataSender);

    DataSender getDataSender();

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
