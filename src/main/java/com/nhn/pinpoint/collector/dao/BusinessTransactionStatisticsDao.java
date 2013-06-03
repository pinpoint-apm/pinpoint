package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.common.dto2.thrift.Span;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public interface BusinessTransactionStatisticsDao {
	void update(Span span);
}
