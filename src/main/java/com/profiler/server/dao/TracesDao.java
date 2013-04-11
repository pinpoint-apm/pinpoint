package com.profiler.server.dao;

import com.profiler.common.dto2.thrift.SpanEvent;
import com.profiler.common.dto2.thrift.Span;
import com.profiler.common.dto2.thrift.SpanChunk;

public interface TracesDao {
    void insert(Span span);

    void insertEvent(SpanEvent spanEvent);

    void insertSpanChunk(SpanChunk spanChunk);
}
