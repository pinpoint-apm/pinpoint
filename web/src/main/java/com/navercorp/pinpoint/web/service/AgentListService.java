package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntryAndStatus;

import java.util.List;

public interface AgentListService {

    List<AgentListEntryAndStatus> getAgentList(ServiceUid serviceUid, ApplicationUid applicationUid);

    List<AgentListEntryAndStatus> getActiveAgentList(ServiceUid serviceUid, ApplicationUid applicationUid, Range range);

    void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);
}
