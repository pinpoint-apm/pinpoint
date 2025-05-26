package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface AgentNameDao {
    void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId, long agentStartTime, String agentName);

    List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid, ApplicationUid applicationUid);

    List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);

    void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid, List<AgentIdentifier> agents);
}
