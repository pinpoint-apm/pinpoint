package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;

import java.util.List;

public interface AgentIdDao {

    List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName);

    List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode);

    List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode, String agentId);

    List<AgentIdEntry> getAgentIdEntryByInsertTimeAfter(int serviceUid, String applicationName, int serviceTypeCode, long minUpdateTimestamp);

    void delete(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime);

    void delete(List<AgentIdEntry> agentIdEntryList);

    void insert(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime, String agentName, long timestamp);
}
