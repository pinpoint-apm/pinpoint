package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface AgentIdDao {
    void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId);

    List<String> scanAgentId(ServiceUid serviceUid, ApplicationUid applicationUid);

    List<List<String>> scanAgentId(ServiceUid serviceUid, List<ApplicationUid> applicationUidList);

    void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid, List<String> agentIdList);
}
