package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SpanChunk;
import com.profiler.common.dto.thrift.SubSpan;

public interface TracesDao {
    void insert(String applicationName, Span span);

    void insertSubSpan(String applicationName, SubSpan subSpan);

    void insertSubSpanList(String applicationName, SpanChunk spanChunk);
}
