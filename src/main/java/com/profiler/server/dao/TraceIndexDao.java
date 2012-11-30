package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

public interface TraceIndexDao {
	void insert(Span span);
}
