package com.nhn.pinpoint.web.dao;

import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationMapStatisticsCalleeDao {
	public List<TransactionFlowStatistics> selectCallee(String callerApplicationName, short callerServiceType, long from, long to);

	public List<Map<Long, Map<Short, Long>>> selectCalleeStatistics(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, long from, long to);
}
