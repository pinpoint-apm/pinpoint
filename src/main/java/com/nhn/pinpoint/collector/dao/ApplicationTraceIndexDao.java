package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TSpan;

public interface ApplicationTraceIndexDao {
	void insert(TSpan span);
}
