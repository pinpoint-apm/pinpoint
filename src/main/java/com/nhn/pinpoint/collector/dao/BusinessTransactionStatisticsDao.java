package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TSpan;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public interface BusinessTransactionStatisticsDao {
	void update(TSpan span);
}
