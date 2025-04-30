package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;

import java.util.List;

public interface AgentListService {
    List<AgentListEntry> getAllAgentList(ServiceUid serviceUid);

    List<AgentListEntry> getAllAgentList(ServiceUid serviceUid, Range range);

    List<AgentListEntry> getApplicationAgentList(ServiceUid serviceUid, ApplicationUid applicationUid);

    List<AgentListEntry> getApplicationAgentList(ServiceUid serviceUid, ApplicationUid applicationUid, Range range);

    int cleanupInactiveAgent(ServiceUid serviceUid, ApplicationUid applicationUid, Range range);
}
