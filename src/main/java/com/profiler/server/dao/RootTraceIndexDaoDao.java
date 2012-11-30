package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

public interface RootTraceIndexDaoDao {
    void insert(Span rootSpan);
}
