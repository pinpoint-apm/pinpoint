package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface AgentNameService {

    List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid);

    List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid, ApplicationUid applicationUid);

    List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);

    void deleteAllAgents(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);
}
