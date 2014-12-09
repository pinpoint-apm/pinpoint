package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.thrift.dto.TSpan;

/**
 * @author emeroad
 */
public interface ApplicationTraceIndexDao {
	void insert(TSpan span);
}
