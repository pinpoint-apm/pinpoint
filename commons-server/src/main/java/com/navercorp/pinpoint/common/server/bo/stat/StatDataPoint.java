package com.navercorp.pinpoint.common.server.bo.stat;

public interface StatDataPoint {

    DataPoint getDataPoint();

    AgentStatType getAgentStatType();
}
