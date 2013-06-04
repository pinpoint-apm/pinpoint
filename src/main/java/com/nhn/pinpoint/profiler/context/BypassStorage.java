package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 *
 */
public class BypassStorage implements Storage {
    private DataSender dataSender;

    public BypassStorage() {
    }

    public BypassStorage(DataSender dataSender) {
        this.dataSender = dataSender;
    }

    @Override
    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
    }

    @Override
    public DataSender getDataSender() {
        return dataSender;
    }

    @Override
    public void store(SpanEvent spanEvent) {
        dataSender.send(spanEvent);
    }

    @Override
    public void store(Span span) {
        dataSender.send(span);
    }
}
