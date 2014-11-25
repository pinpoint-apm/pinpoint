package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.collector.dao.MapStatisticsCalleeDao;
import com.nhn.pinpoint.collector.dao.MapStatisticsCallerDao;
import com.nhn.pinpoint.collector.dao.MapResponseTimeDao;
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
	private MapStatisticsCalleeDao mapStatisticsCalleeDao;

	@Autowired
	private MapStatisticsCallerDao mapStatisticsCallerDao;

    @Autowired
    private MapResponseTimeDao mapResponseTimeDao;

    /**
     * tomcat에서 mysql을 호출하였을 경우 아래와 같이 로그가 남는다. <br/>
     * emeroad-app (TOMCAT) -> MySQL_DB_ID (MYSQL)[10.25.141.69:3306] <br/>
     * <br/>
     * callee에서는 <br/>
     * MySQL (MYSQL) <- emeroad-app (TOMCAT)[localhost:8080]
     * @param callerApplicationName
     * @param callerServiceType
     * @param calleeApplicationName
     * @param calleeServiceType
     * @param calleeHost
     * @param elapsed
     * @param isError
     */
	public void updateCaller(String callerApplicationName, short callerServiceType, String callerAgentId, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
		mapStatisticsCallerDao.update(callerApplicationName, callerServiceType, callerAgentId, calleeApplicationName, calleeServiceType, calleeHost, elapsed, isError);
	}

    /**
     * tomcat에서 mysql을 호출하였을 경우 아래와 같이 로그가 남는다. <br/>
     * MySQL_DB_ID (MYSQL) <- emeroad-app (TOMCAT)[localhost:8080] <br/>
     * <br/><br/>
     * caller에서는 <br/>
     * emeroad-app (TOMCAT) -> MySQL (MYSQL)[10.25.141.69:3306]
     * @param callerApplicationName
     * @param callerServiceType
     * @param calleeApplicationName
     * @param calleeServiceType
     * @param callerHost
     * @param elapsed
     * @param isError
     */
	public void updateCallee(String calleeApplicationName, short calleeServiceType, String callerApplicationName, short callerServiceType, String callerHost, int elapsed, boolean isError) {
		mapStatisticsCalleeDao.update(calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, callerHost, elapsed, isError);
	}

    public void updateResponseTime(String applicationName, short serviceType, String agentId, int elapsed, boolean isError) {
        mapResponseTimeDao.received(applicationName, serviceType, agentId, elapsed, isError);
    }
}
