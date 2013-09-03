package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.Span;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public interface BusinessTransactionStatisticsDao {
	void update(Span span);
}
