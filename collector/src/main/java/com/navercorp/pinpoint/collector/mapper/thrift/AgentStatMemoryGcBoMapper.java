package com.nhn.pinpoint.collector.mapper.thrift;

import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author hyungil.jeong
 */
@Component
public class AgentStatMemoryGcBoMapper implements ThriftBoMapper<AgentStatMemoryGcBo, TAgentStat> {

    @Override
    public AgentStatMemoryGcBo map(TAgentStat thriftObject) {
        final String agentId = thriftObject.getAgentId();
        final long startTimestamp = thriftObject.getStartTimestamp();
        final long timestamp = thriftObject.getTimestamp();
        final TJvmGc gc = thriftObject.getGc();

        final AgentStatMemoryGcBo.Builder builder = new AgentStatMemoryGcBo.Builder(agentId, startTimestamp, timestamp);
        // gc is optional (for now, null check is enough for non-primitives)
        if (gc != null) {
            builder.gcType(gc.getType().name());
            builder.jvmMemoryHeapUsed(gc.getJvmMemoryHeapUsed());
            builder.jvmMemoryHeapMax(gc.getJvmMemoryHeapMax());
            builder.jvmMemoryNonHeapUsed(gc.getJvmMemoryNonHeapUsed());
            builder.jvmMemoryNonHeapMax(gc.getJvmMemoryNonHeapMax());
            builder.jvmGcOldCount(gc.getJvmGcOldCount());
            builder.jvmGcOldTime(gc.getJvmGcOldTime());
        } else {
            builder.gcType(TJvmGcType.UNKNOWN.name());
        }
        return builder.build();
    }

}
