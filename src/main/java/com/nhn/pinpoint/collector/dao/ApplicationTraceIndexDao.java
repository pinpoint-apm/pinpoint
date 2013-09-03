package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.Span;

public interface ApplicationTraceIndexDao {
	void insert(Span span);
}
