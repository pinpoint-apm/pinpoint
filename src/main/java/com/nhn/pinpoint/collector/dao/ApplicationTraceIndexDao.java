package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TSpan;

/**
 * @author emeroad
 */
public interface ApplicationTraceIndexDao {
	void insert(TSpan span);
}
