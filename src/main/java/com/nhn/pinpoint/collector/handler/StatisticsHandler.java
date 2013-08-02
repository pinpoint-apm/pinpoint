package com.nhn.pinpoint.collector.handler;

import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.collector.dao.ApplicationStatisticsDao;

/**
 * 
 * @author netspider
 * 
 */
public class StatisticsHandler {

	@Autowired
	private ApplicationStatisticsDao applicationMapStatisticsDao;

	@Autowired
	private ApplicationMapStatisticsCallerDao applicationMapStatisticsCallerDao;

	@Autowired
	private ApplicationMapStatisticsCalleeDao applicationMapStatisticsCalleeDao;

	public void updateCallee(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
		applicationMapStatisticsCalleeDao.update(callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType, calleeHost, elapsed, isError);
	}

	public void updateCaller(String calleeApplicationName, short calleeServiceType, String callerApplicationName, short callerServiceType, String callerHost, int elapsed, boolean isError) {
		applicationMapStatisticsCallerDao.update(calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, callerHost, elapsed, isError);
	}

	public void updateApplication(String applicationName, short serviceType, String agentId, int elapsed, boolean isError) {
		applicationMapStatisticsDao.update(applicationName, serviceType, agentId, elapsed, isError);
	}
}
