package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.agent.AgentListItem;

import java.util.List;

public interface AgentListV2Service {

    List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName);

    List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType);

    List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, Range range);

    List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range);
}
