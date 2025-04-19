package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;

import java.util.List;

public interface AgentListDao {

    List<AgentListEntry> selectAgentListEntry(ServiceUid serviceUid, ApplicationUid applicationUid);

    List<AgentListEntry> selectAgentListEntry(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);

    void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid, List<AgentListEntry> agentList);
}
