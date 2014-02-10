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
public interface ApplicationMapStatisticsCalleeDao {
	public List<TransactionFlowStatistics> selectCallee(Application callerApplication, long from, long to);

	public List<Map<Long, Map<Short, Long>>> selectCalleeStatistics(Application callerApplication, Application calleeApplication, long from, long to);
}
