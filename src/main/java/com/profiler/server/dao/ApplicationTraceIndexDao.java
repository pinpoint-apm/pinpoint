package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

public interface ApplicationTraceIndexDao {
	void insert(String applicationName, Span span);
}
