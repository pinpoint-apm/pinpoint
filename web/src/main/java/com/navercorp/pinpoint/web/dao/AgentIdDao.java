package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface AgentIdDao {
    List<String> getAgentIds(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    List<String> getAgentIds(ServiceUid serviceUid, String applicationName);

    List<String> getAgentIds(ServiceUid serviceUid, String applicationName, int serviceTypeCode, long maxTimestamp);

    void deleteAllAgents(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    void deleteAgents(ServiceUid serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList);

    void insert(ServiceUid serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList);
}
