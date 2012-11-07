package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

public interface ApplicationTraceIndex {
	void insert(String applicationName, Span span);
}
