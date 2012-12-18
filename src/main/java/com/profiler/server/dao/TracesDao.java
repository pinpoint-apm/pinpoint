package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.common.dto.thrift.SubSpanList;

public interface TracesDao {
    void insert(String applicationName, Span span);

    void insertSubSpan(String applicationName, SubSpan subSpan);

    void insertSubSpanList(String applicationName, SubSpanList subSpanList);
}
