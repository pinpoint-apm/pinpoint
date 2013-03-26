package com.nhn.hippo.web.dao;

import java.util.Map;

import com.nhn.hippo.web.applicationmap.ApplicationStatistics;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationMapStatisticsCallerDao {
	public Map<String, ApplicationStatistics> selectCaller(String calleeApplicationName, short calleeServiceType, long from, long to);
}
