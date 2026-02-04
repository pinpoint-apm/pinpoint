package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntryAndStatus;

import java.util.List;

public interface AgentListV2Service {

    List<AgentIdEntryAndStatus> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType);

    List<AgentIdEntryAndStatus> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range);

}
