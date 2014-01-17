package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCallerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author netspider
 * 
 */
@Service
public class StatisticsHandler {

	@Autowired
	private ApplicationMapStatisticsCalleeDao applicationMapStatisticsCalleeDao;

	@Autowired
	private ApplicationMapStatisticsCallerDao applicationMapStatisticsCallerDao;

	public void updateCaller(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
		applicationMapStatisticsCallerDao.update(calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, calleeHost, elapsed, isError);
	}

	public void updateCallee(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String callerHost, int elapsed, boolean isError) {
		applicationMapStatisticsCalleeDao.update(calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, callerHost, elapsed, isError);
	}
}
