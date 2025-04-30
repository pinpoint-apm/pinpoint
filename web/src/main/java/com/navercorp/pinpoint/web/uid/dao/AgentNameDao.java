package com.navercorp.pinpoint.web.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;

import java.util.List;

public interface AgentNameDao {

    List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid);

    List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid, ApplicationUid applicationUid);

    List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);

    void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid, List<AgentIdentifier> agentList);
}
