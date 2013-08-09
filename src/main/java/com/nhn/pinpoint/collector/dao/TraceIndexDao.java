package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.common.dto2.thrift.Span;

@Deprecated
public interface TraceIndexDao {
	void insert(Span span);
}
