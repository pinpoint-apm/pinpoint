/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.dao.MapResponseTimeDao;
import com.navercorp.pinpoint.collector.dao.MapStatisticsCalleeDao;
import com.navercorp.pinpoint.collector.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.common.trace.ServiceType;

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
     * Calling MySQL from Tomcat generates the following message for the caller(Tomcat) :<br/>
     * emeroad-app (TOMCAT) -> MySQL_DB_ID (MYSQL)[10.25.141.69:3306] <br/>
     * <br/>
     * The following message is generated for the callee(MySQL) :<br/>
     * MySQL (MYSQL) <- emeroad-app (TOMCAT)[localhost:8080]
     * @param callerApplicationName
     * @param callerServiceType
     * @param calleeApplicationName
     * @param calleeServiceType
     * @param calleeHost
     * @param elapsed
     * @param isError
     */
    public void updateCaller(String callerApplicationName, ServiceType callerServiceType, String callerAgentId, String calleeApplicationName, ServiceType calleeServiceType, String calleeHost, int elapsed, boolean isError) {
        mapStatisticsCallerDao.update(callerApplicationName, callerServiceType, callerAgentId, calleeApplicationName, calleeServiceType, calleeHost, elapsed, isError);
    }

    /**
     * Calling MySQL from Tomcat generates the following message for the callee(MySQL) :<br/>
     * MySQL_DB_ID (MYSQL) <- emeroad-app (TOMCAT)[localhost:8080] <br/>
     * <br/><br/>
     * The following message is generated for the caller(Tomcat) :<br/>
     * emeroad-app (TOMCAT) -> MySQL (MYSQL)[10.25.141.69:3306]
     * @param callerApplicationName
     * @param callerServiceType
     * @param calleeApplicationName
     * @param calleeServiceType
     * @param callerHost
     * @param elapsed
     * @param isError
     */
    public void updateCallee(String calleeApplicationName, ServiceType calleeServiceType, String callerApplicationName, ServiceType callerServiceType, String callerHost, int elapsed, boolean isError) {
        mapStatisticsCalleeDao.update(calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, callerHost, elapsed, isError);
    }

    public void updateResponseTime(String applicationName, ServiceType serviceType, String agentId, int elapsed, boolean isError) {
        mapResponseTimeDao.received(applicationName, serviceType, agentId, elapsed, isError);
    }
}
