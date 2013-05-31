package com.profiler.server.dao;

import com.nhn.pinpoint.common.dto2.thrift.SpanEvent;
import com.nhn.pinpoint.common.dto2.thrift.Span;
import com.nhn.pinpoint.common.dto2.thrift.SpanChunk;

public interface TracesDao {
    void insert(Span span);

    void insertEvent(SpanEvent spanEvent);

    void insertSpanChunk(SpanChunk spanChunk);
}
