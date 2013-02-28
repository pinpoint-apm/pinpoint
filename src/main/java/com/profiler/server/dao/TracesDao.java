package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Event;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SpanChunk;

public interface TracesDao {
    void insert(String applicationName, Span span);

    void insertEvent(String applicationName, Event event);

    void insertSpanChunk(String applicationName, SpanChunk spanChunk);
}
