package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

public interface Traces {
    void insert(String applicationName, Span span);
}
