package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface AgentListDao {
    void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId, long agentStartTime, String agentName);
}
