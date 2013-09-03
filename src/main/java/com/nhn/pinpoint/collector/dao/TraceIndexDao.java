package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.Span;

@Deprecated
public interface TraceIndexDao {
	void insert(Span span);
}
