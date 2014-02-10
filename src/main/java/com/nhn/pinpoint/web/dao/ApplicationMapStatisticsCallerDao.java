package com.nhn.pinpoint.web.dao;

import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.vo.Application;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationMapStatisticsCallerDao {
	public List<TransactionFlowStatistics> selectCaller(Application calleeApplication, long from, long to);
	
	public List<Map<Long, Map<Short, Long>>> selectCallerStatistics(Application callerApplication, Application calleeApplication, long from, long to);
}
