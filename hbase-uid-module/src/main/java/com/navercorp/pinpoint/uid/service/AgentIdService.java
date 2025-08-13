package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface AgentIdService {

    void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);

    List<String> getAgentId(ServiceUid serviceUid, ApplicationUid applicationUid);

    List<List<String>> getAgentId(ServiceUid serviceUid, List<ApplicationUid> applicationUidList);

    void deleteAllAgent(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);

    void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, List<String> agentIdList);
}
