package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;

public interface ApplicationIndexServiceV2 {

    List<Application> getApplications(String serviceName);

    List<Application> getApplications(String serviceName, String applicationName);

    List<String> getAgentIds(String serviceName, String applicationName, int serviceTypeCode);

    void deleteApplication(String serviceName, String applicationName, int serviceTypeCode);

    void deleteAllAgents(String serviceName, String applicationName, int serviceTypeCode);

    void deleteAgents(String serviceName, String applicationName, int serviceTypeCode, List<String> agentIdList);

}
