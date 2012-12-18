package com.profiler.context;

import com.profiler.sender.DataSender;

/**
 *
 */
public interface Storage {

    void setDataSender(DataSender dataSender);

    DataSender getDataSender();

    void store(SubSpan subSpan);

    void store(Span span);
}
