package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.collector.service.async.AgentProperty;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;

public interface ApplicationIndexPerTimeDao {
    void insert(final AgentLifeCycleBo agentLifeCycleBo, AgentProperty agentProperty);
}
