package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

public interface AgentIdDao {
    void insert(int serviceUid, AgentInfoBo agentInfoBo);

    void insert(int serviceUid, int serviceTypeCode, AgentInfoBo agentInfoBo);

    void updateState(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime,
                     long eventTimestamp, AgentLifeCycleState agentLifeCycleState);
}
