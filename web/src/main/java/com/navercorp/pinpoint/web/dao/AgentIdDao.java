package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface AgentIdDao {
    List<String> scanAgentId(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    List<String> scanAgentId(ServiceUid serviceUid, String applicationName);

    void deleteAgents(ServiceUid serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList);

    void insert(ServiceUid serviceUid, String applicationName, int serviceTypeCode, String agentId);
}
