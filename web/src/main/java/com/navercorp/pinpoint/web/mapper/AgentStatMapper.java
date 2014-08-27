package com.nhn.pinpoint.web.mapper;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_STAT_CF_STATISTICS;
import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_STAT_CF_STATISTICS_V1;
import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_STAT_CF_STATISTICS_MEMORY_GC;
import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_STAT_CF_STATISTICS_CPU_LOAD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.web.vo.AgentStat;

import org.apache.hadoop.hbase.client.Result;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author harebox
 * @author hyungil.jeong
 */
@Component
public class AgentStatMapper implements RowMapper<List<AgentStat>> {

    private TProtocolFactory factory = new TCompactProtocol.Factory();

    public List<AgentStat> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        Map<byte[], byte[]> qualifierMap = result.getFamilyMap(AGENT_STAT_CF_STATISTICS);
        // FIXME (2014.08) Legacy support for TAgentStat Thrift DTO stored directly into hbase.
        if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_V1)) {
            return readAgentStatThriftDto(qualifierMap.get(AGENT_STAT_CF_STATISTICS_V1));
        }

        AgentStat agentStat = new AgentStat();
        if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_MEMORY_GC)) {
            agentStat.setMemoryGc(new AgentStatMemoryGcBo.Builder(qualifierMap.get(AGENT_STAT_CF_STATISTICS_MEMORY_GC)).build());
        }
        if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_CPU_LOAD)) {
            agentStat.setCpuLoad(new AgentStatCpuLoadBo.Builder(qualifierMap.get(AGENT_STAT_CF_STATISTICS_CPU_LOAD)).build());
        }
        List<AgentStat> agentStats = new ArrayList<AgentStat>();
        agentStats.add(agentStat);
        return agentStats;
    }

    // FIXME (2014.08) Legacy support for TAgentStat Thrift DTO stored directly into hbase.
    private List<AgentStat> readAgentStatThriftDto(byte[] tAgentStatByteArray) throws TException {
        // CompactProtocol을 사용하고 있음.
        TDeserializer deserializer = new TDeserializer(factory);
        TAgentStat tAgentStat = new TAgentStat();
        deserializer.deserialize(tAgentStat, tAgentStatByteArray);
        TJvmGc gc = tAgentStat.getGc();

        AgentStatMemoryGcBo.Builder memoryGcBoBuilder = new AgentStatMemoryGcBo.Builder(tAgentStat.getAgentId(), tAgentStat.getStartTimestamp(), tAgentStat.getTimestamp());
        memoryGcBoBuilder.gcType(gc.getType().name());
        memoryGcBoBuilder.jvmMemoryHeapUsed(gc.getJvmMemoryHeapUsed()).jvmMemoryHeapMax(gc.getJvmMemoryHeapMax());
        memoryGcBoBuilder.jvmMemoryNonHeapUsed(gc.getJvmMemoryNonHeapUsed()).jvmMemoryNonHeapMax(gc.getJvmMemoryNonHeapMax());
        memoryGcBoBuilder.jvmGcOldCount(gc.getJvmGcOldCount()).jvmGcOldTime(gc.getJvmGcOldTime());

        AgentStat agentStat = new AgentStat();
        agentStat.setMemoryGc(memoryGcBoBuilder.build());
        List<AgentStat> result = new ArrayList<AgentStat>(1);
        result.add(agentStat);
        return result;
    }

}
