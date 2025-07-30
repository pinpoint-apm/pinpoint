package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;

import java.util.List;

public interface AgentListService {

    List<AgentListEntry> getApplicationAgentList(String serviceName, String applicationName);
    List<AgentListEntry> getApplicationAgentList(String serviceName, String applicationName, Range range);

    List<AgentListEntry> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode);
    List<AgentListEntry> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode, Range range);

    int cleanupInactiveAgent(String serviceName, String applicationName, int serviceTypeCode, Range range);
}
