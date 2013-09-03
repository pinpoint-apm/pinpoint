package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.SpanEvent;
import com.nhn.pinpoint.thrift.dto.Span;
import com.nhn.pinpoint.thrift.dto.SpanChunk;

public interface TracesDao {
    void insert(Span span);

    void insertEvent(SpanEvent spanEvent);

    void insertSpanChunk(SpanChunk spanChunk);
}
