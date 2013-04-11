package com.profiler.server.dao;

import com.profiler.common.dto2.thrift.Span;

public interface ApplicationTraceIndexDao {
	void insert(Span span);
}
