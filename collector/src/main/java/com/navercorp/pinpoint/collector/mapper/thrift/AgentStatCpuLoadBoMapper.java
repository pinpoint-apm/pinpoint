package com.nhn.pinpoint.collector.mapper.thrift;

import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TCpuLoad;

/**
 * @author hyungil.jeong
 */
@Component
public class AgentStatCpuLoadBoMapper implements ThriftBoMapper<AgentStatCpuLoadBo, TAgentStat> {

    @Override
    public AgentStatCpuLoadBo map(TAgentStat thriftObject) {
        final String agentId = thriftObject.getAgentId();
        final long startTimestamp = thriftObject.getStartTimestamp();
        final long timestamp = thriftObject.getTimestamp();
        final TCpuLoad cpuLoad = thriftObject.getCpuLoad();

        final AgentStatCpuLoadBo.Builder builder = new AgentStatCpuLoadBo.Builder(agentId, startTimestamp, timestamp);
        // cpuLoad is optional (for now, null check is enough for non-primitives)
        if (cpuLoad != null) {
            // jvmCpuLoad is optional
            if (cpuLoad.isSetJvmCpuLoad()) {
                builder.jvmCpuLoad(cpuLoad.getJvmCpuLoad());
            }
            // systemCpuLoad is optional
            if (cpuLoad.isSetSystemCpuLoad()) {
                builder.systemCpuLoad(cpuLoad.getSystemCpuLoad());
            }
        }
        return builder.build();
    }

}
