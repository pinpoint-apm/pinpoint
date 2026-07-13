package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Service;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;

import java.util.List;

public interface AgentListV2Service {

    List<AgentIdEntry> getAllAgentList(Service service, String applicationName, ServiceType serviceType);

    List<AgentIdEntry> getActiveAgentList(Service service, String applicationName, ServiceType serviceType, Range range);

}
