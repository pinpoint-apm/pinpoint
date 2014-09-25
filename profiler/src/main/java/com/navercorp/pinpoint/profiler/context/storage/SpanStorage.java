package com.nhn.pinpoint.profiler.context.storage;

import com.nhn.pinpoint.profiler.context.Span;
import com.nhn.pinpoint.profiler.context.SpanEvent;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanStorage implements Storage {

    protected List<TSpanEvent> spanEventList = new ArrayList<TSpanEvent>(10);
    private final DataSender dataSender;

    public SpanStorage(DataSender dataSender) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        this.dataSender = dataSender;
    }

    @Override
    public void store(SpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        final List<TSpanEvent> spanEventList = this.spanEventList;
        if (spanEventList != null) {
            spanEventList.add(spanEvent);
        } else {
            throw new IllegalStateException("spanEventList is null");
        }
    }

    @Override
    public void store(Span span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        span.setSpanEventList(spanEventList);
        spanEventList = null;
        this.dataSender.send(span);
    }
}
