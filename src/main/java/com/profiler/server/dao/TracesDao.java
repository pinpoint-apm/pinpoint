package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SubSpan;

public interface TracesDao {
    void insert(String applicationName, Span span);

    void insertTerminalSpan(String applicationName, SubSpan span);
}
