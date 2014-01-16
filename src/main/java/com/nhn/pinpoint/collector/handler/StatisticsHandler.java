package com.nhn.pinpoint.collector.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCallerDao;

/**
 * 
 * @author netspider
 * 
 */
@Service
public class StatisticsHandler {

	@Autowired
	private ApplicationMapStatisticsCallerDao applicationMapStatisticsCallerDao;

	@Autowired
	private ApplicationMapStatisticsCalleeDao applicationMapStatisticsCalleeDao;

	public void updateCallee(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
		applicationMapStatisticsCalleeDao.update(calleeApplicationName, calleeServiceType, calleeHost, callerApplicationName, callerServiceType, elapsed, isError);
	}

	public void updateCaller(String calleeApplicationName, short calleeServiceType, String callerApplicationName, short callerServiceType, String callerHost, int elapsed, boolean isError) {
		applicationMapStatisticsCallerDao.update(callerApplicationName, callerServiceType, callerHost, calleeApplicationName, calleeServiceType, elapsed, isError);
	}
}
