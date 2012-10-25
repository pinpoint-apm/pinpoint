package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

/**
 *
 */
public interface RootTraceIndexDao {
    void insert(Span rootSpan);
}
