package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

/**
 *
 */
public interface TraceDao {
    void insert(Span span);
}
