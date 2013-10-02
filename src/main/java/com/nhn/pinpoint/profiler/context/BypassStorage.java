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
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        this.dataSender = dataSender;
    }

    @Override
    public void setDataSender(DataSender dataSender) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        this.dataSender = dataSender;
    }

    @Override
    public DataSender getDataSender() {
        return dataSender;
    }

    @Override
    public void store(SpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        dataSender.send(spanEvent);
    }

    @Override
    public void store(Span span) {
        dataSender.send(span);
    }
}
