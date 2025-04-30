package com.navercorp.pinpoint.collector.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface AgentNameDao {
    void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId, long agentStartTime, String agentName);
}
