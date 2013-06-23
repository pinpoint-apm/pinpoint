package com.nhn.pinpoint.web.dao;

import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.web.applicationmap.TransactionFlowStatistics;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationMapStatisticsCallerDao {
	public Map<String, TransactionFlowStatistics> selectCaller(String calleeApplicationName, short calleeServiceType, long from, long to);
	
	public List<Map<Long, Map<Short, Long>>> selectCallerStatistics(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, long from, long to);
}
