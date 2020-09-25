package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;

public interface GrpcStatMapper {
    void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat);
}
