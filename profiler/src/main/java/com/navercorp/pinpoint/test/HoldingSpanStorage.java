package com.navercorp.pinpoint.test;

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.storage.Storage;

/**
 * @author hyungil.jeong
 */
public final class HoldingSpanStorage implements Storage {

    private final PeekableDataSender<? extends TBase<?, ?>> dataSender;

    public HoldingSpanStorage(PeekableDataSender<? extends TBase<?, ?>> dataSender) {
        this.dataSender = dataSender;
    }

    @Override
    public void store(SpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        this.dataSender.send(spanEvent);
    }

    @Override
    public void store(Span span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        this.dataSender.send(span);
    }

}
