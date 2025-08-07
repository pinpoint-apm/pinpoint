package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;

import java.util.List;

public interface AgentListService {

    List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName);

    List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName, Range range);

    List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode);

    List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode, Range range);

    int cleanupInactiveAgent(String serviceName, String applicationName, int serviceTypeCode, Range range);
}
