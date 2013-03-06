package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;

/**
 * 
 * @author netspider
 * 
 */
public interface BusinessTransactionStatisticsDao {
	void update(Span span);
}
