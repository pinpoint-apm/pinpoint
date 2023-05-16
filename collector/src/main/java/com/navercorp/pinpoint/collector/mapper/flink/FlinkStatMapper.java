package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;


public interface FlinkStatMapper<T extends AgentStatDataPoint, U extends TFAgentStat> {
    void map(T t, U u);

    void build(TFAgentStatMapper.TFAgentStatBuilder builder);
}
