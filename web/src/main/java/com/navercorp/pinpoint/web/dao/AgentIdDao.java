package com.navercorp.pinpoint.web.dao;

import java.util.List;

public interface AgentIdDao {
    List<String> getAgentIds(int serviceUid, String applicationName, int serviceTypeCode);

    List<String> getAgentIds(int serviceUid, String applicationName);

    List<String> getAgentIds(int serviceUid, String applicationName, int serviceTypeCode, long maxTimestamp);

    void deleteAllAgents(int serviceUid, String applicationName, int serviceTypeCode);

    void deleteAgents(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList);

    void insert(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList);
}
