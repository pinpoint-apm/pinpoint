package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.applicationmap.rawdata.ApplicationStatistics;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationStatisticsDao {

	public ApplicationStatistics selectApplicationStatistics(String applicationName, short serviceType, long from, long to);
}
