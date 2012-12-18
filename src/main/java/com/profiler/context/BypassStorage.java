package com.profiler.context;

import com.profiler.sender.DataSender;

/**
 *
 */
public class BypassStorage implements Storage {
    private DataSender dataSender;

    @Override
    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
    }

    @Override
    public DataSender getDataSender() {
        return dataSender;
    }

    @Override
    public void store(SubSpan subSpan) {
        dataSender.send(subSpan);
    }

    @Override
    public void store(Span span) {
        dataSender.send(span);
    }
}
