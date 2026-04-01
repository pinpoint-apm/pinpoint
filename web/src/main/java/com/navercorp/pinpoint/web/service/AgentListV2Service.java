package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;

import java.util.List;

public interface AgentListV2Service {

    List<AgentIdEntry> getAllAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType);

    List<AgentIdEntry> getActiveAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range);

}
