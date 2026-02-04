package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;

public interface AgentIdDao {
    void insert(int serviceUid, AgentInfoBo agentInfoBo);
}
