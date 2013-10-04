package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TSpan;

@Deprecated
public interface TraceIndexDao {
	void insert(TSpan span);
}
