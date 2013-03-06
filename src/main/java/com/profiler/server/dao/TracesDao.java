package com.profiler.server.dao;

import com.profiler.common.dto.thrift.SpanEvent;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SpanChunk;

public interface TracesDao {
    void insert(Span span);

    void insertEvent(SpanEvent spanEvent);

    void insertSpanChunk(SpanChunk spanChunk);
}
