package com.nhn.pinpoint.server.dao;

import com.nhn.pinpoint.common.dto2.thrift.Span;

public interface ApplicationTraceIndexDao {
	void insert(Span span);
}
