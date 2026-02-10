package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.vo.agent.AgentListItem;

import java.util.List;

public interface AgentIdDao {

    List<AgentListItem> getAgentListItems(int serviceUid, String applicationName);

    List<AgentListItem> getAgentListItems(int serviceUid, String applicationName, int serviceTypeCode);

    List<AgentListItem> getAgentListItems(int serviceUid, String applicationName, int serviceTypeCode, String agentId);

    List<AgentListItem> getAgentListItems(int serviceUid, String applicationName, int serviceTypeCode, long maxTimestamp);

    void delete(int serviceUid, String applicationName, int serviceTypeCode, String agentId);

    void insert(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime, String agentName, long timestamp);
}
