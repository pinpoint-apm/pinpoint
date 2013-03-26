package com.nhn.hippo.web.dao;

import java.util.Map;

import com.nhn.hippo.web.applicationmap.ApplicationStatistics;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationMapStatisticsCalleeDao {
	public Map<String, ApplicationStatistics> selectCallee(String callerApplicationName, short callerServiceType, long from, long to);
}
