package com.profiler.context;

import com.profiler.sender.DataSender;

/**
 *
 */
public interface Storage {

    void setDataSender(DataSender dataSender);

    DataSender getDataSender();

    /**
     * store(Event subSpan)와 store(Span span)간 동기화가 구현되어 있어야 한다.
     *
     * @param subSpan
     */
    void store(SubSpan subSpan);

    /**
     * store(Event subSpan)와 store(Span span)간 동기화가 구현되어 있어야 한다.
     *
     * @param span
     */
    void store(Span span);
}
