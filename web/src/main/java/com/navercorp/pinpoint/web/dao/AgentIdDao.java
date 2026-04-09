package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface AgentIdDao {

    List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName);

    List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode);

    List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode, String agentId);

    List<AgentIdEntry> getAgentIdEntryByMinStateTimestamp(int serviceUid, String applicationName, int serviceTypeCode, long minStateTimestamp);

    void delete(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime);

    void delete(List<AgentIdEntry> agentIdEntryList);

    void insert(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime,
                String agentName, @Nullable AgentStatus agentStatus);

    int countAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode);

    List<AgentIdEntry> getInactiveAgentIdEntry(long maxStatusTimestamp, int limit, @Nullable AgentIdEntry lastAgentIdEntry);
}
