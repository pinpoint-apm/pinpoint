package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface AgentIdDao {
    void insert(ServiceUid serviceUid, AgentInfoBo agentInfo);
}
