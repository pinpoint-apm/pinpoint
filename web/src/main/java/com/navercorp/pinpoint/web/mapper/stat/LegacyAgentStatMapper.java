/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.mapper.stat;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import com.navercorp.pinpoint.common.server.bo.ActiveTraceHistogramBo;
import com.navercorp.pinpoint.common.server.bo.AgentStatCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.AgentStatMemoryGcBo;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author harebox
 * @author HyunGil Jeong
 */
@Deprecated
@Component("legacyAgentStatMapper")
public class LegacyAgentStatMapper implements RowMapper<List<AgentStat>> {

    private TProtocolFactory factory = new TCompactProtocol.Factory();

    @Autowired
    @Qualifier("agentStatRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    public List<AgentStat> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());
        final String agentId = BytesUtils.toString(rowKey, 0, AGENT_NAME_MAX_LEN).trim();
        final long reverseTimestamp = BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN);
        final long timestamp = TimeUtils.recoveryTimeMillis(reverseTimestamp);

        NavigableMap<byte[], byte[]> qualifierMap = result.getFamilyMap(AGENT_STAT_CF_STATISTICS);
        if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_V1)) {
            // FIXME (2014.08) Legacy support for TAgentStat Thrift DTO stored directly into hbase.
            return readAgentStatThriftDto(agentId, timestamp, qualifierMap.get(AGENT_STAT_CF_STATISTICS_V1));
        } else if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_MEMORY_GC) || qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_CPU_LOAD)) {
            // FIXME (2015.10) Legacy column for storing serialzied Bos separately.
            return readSerializedBos(agentId, timestamp, qualifierMap);
        }

        AgentStat agentStat = new AgentStat(agentId, timestamp);
        if (qualifierMap.containsKey(AGENT_STAT_COL_INTERVAL)) {
            agentStat.setCollectInterval(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_INTERVAL)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_GC_TYPE)) {
            agentStat.setGcType(Bytes.toString(qualifierMap.get(AGENT_STAT_COL_GC_TYPE)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_GC_OLD_COUNT)) {
            agentStat.setGcOldCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_GC_OLD_COUNT)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_GC_OLD_TIME)) {
            agentStat.setGcOldTime(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_GC_OLD_TIME)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_HEAP_USED)) {
            agentStat.setHeapUsed(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_HEAP_USED)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_HEAP_MAX)) {
            agentStat.setHeapMax(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_HEAP_MAX)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_NON_HEAP_USED)) {
            agentStat.setNonHeapUsed(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_NON_HEAP_USED)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_NON_HEAP_MAX)) {
            agentStat.setNonHeapMax(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_NON_HEAP_MAX)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_JVM_CPU)) {
            agentStat.setJvmCpuUsage(Bytes.toDouble(qualifierMap.get(AGENT_STAT_COL_JVM_CPU)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_SYS_CPU)) {
            agentStat.setSystemCpuUsage(Bytes.toDouble(qualifierMap.get(AGENT_STAT_COL_SYS_CPU)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW)) {
            agentStat.setSampledNewCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION)) {
            agentStat.setSampledContinuationCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW)) {
            agentStat.setUnsampledNewCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION)) {
            agentStat.setUnsampledContinuationCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION)));
        }
        if (qualifierMap.containsKey(AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM)) {
            ActiveTraceHistogramBo activeTraceHistogramBo = new ActiveTraceHistogramBo(qualifierMap.get(AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM));
            agentStat.setHistogramSchema(BaseHistogramSchema.getDefaultHistogramSchemaByTypeCode(activeTraceHistogramBo.getHistogramSchemaType()));
            agentStat.setActiveTraceCounts(activeTraceHistogramBo.getActiveTraceCountMap());
        }

        List<AgentStat> agentStats = new ArrayList<>();
        agentStats.add(agentStat);
        return agentStats;
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }

    // FIXME (2015.10) Legacy column for storing serialzied Bos separately.
    @Deprecated
    private List<AgentStat> readSerializedBos(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
        AgentStat agentStat = new AgentStat(agentId, timestamp);
        if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_MEMORY_GC)) {
            AgentStatMemoryGcBo.Builder builder = new AgentStatMemoryGcBo.Builder(qualifierMap.get(AGENT_STAT_CF_STATISTICS_MEMORY_GC));
            AgentStatMemoryGcBo agentStatMemoryGcBo = builder.build();
            agentStat.setGcType(agentStatMemoryGcBo.getGcType());
            agentStat.setGcOldCount(agentStatMemoryGcBo.getJvmGcOldCount());
            agentStat.setGcOldTime(agentStatMemoryGcBo.getJvmGcOldTime());
            agentStat.setHeapUsed(agentStatMemoryGcBo.getJvmMemoryHeapUsed());
            agentStat.setHeapMax(agentStatMemoryGcBo.getJvmMemoryHeapMax());
            agentStat.setNonHeapUsed(agentStatMemoryGcBo.getJvmMemoryNonHeapUsed());
            agentStat.setNonHeapMax(agentStatMemoryGcBo.getJvmMemoryNonHeapMax());
        }
        if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_CPU_LOAD)) {
            AgentStatCpuLoadBo.Builder builder = new AgentStatCpuLoadBo.Builder(qualifierMap.get(AGENT_STAT_CF_STATISTICS_CPU_LOAD));
            AgentStatCpuLoadBo agentStatCpuLoadBo = builder.build();
            agentStat.setJvmCpuUsage(agentStatCpuLoadBo.getJvmCpuLoad());
            agentStat.setSystemCpuUsage(agentStatCpuLoadBo.getSystemCpuLoad());
        }
        List<AgentStat> result = new ArrayList<>(1);
        result.add(agentStat);
        return result;
    }

    // FIXME (2014.08) Legacy support for TAgentStat Thrift DTO stored directly into hbase.
    @Deprecated
    private List<AgentStat> readAgentStatThriftDto(String agentId, long timestamp, byte[] tAgentStatByteArray) throws TException {
        // CompactProtocol used
        TDeserializer deserializer = new TDeserializer(factory);
        TAgentStat tAgentStat = new TAgentStat();
        deserializer.deserialize(tAgentStat, tAgentStatByteArray);
        TJvmGc gc = tAgentStat.getGc();
        if (gc == null) {
            return Collections.emptyList();
        }
        AgentStatMemoryGcBo.Builder memoryGcBoBuilder = new AgentStatMemoryGcBo.Builder(tAgentStat.getAgentId(), tAgentStat.getStartTimestamp(), tAgentStat.getTimestamp());
        memoryGcBoBuilder.gcType(gc.getType().name());
        memoryGcBoBuilder.jvmMemoryHeapUsed(gc.getJvmMemoryHeapUsed());
        memoryGcBoBuilder.jvmMemoryHeapMax(gc.getJvmMemoryHeapMax());
        memoryGcBoBuilder.jvmMemoryNonHeapUsed(gc.getJvmMemoryNonHeapUsed());
        memoryGcBoBuilder.jvmMemoryNonHeapMax(gc.getJvmMemoryNonHeapMax());
        memoryGcBoBuilder.jvmGcOldCount(gc.getJvmGcOldCount());
        memoryGcBoBuilder.jvmGcOldTime(gc.getJvmGcOldTime());

        AgentStat agentStat = new AgentStat(agentId, timestamp);
        AgentStatMemoryGcBo agentStatMemoryGcBo = memoryGcBoBuilder.build();
        agentStat.setGcType(agentStatMemoryGcBo.getGcType());
        agentStat.setGcOldCount(agentStatMemoryGcBo.getJvmGcOldCount());
        agentStat.setGcOldTime(agentStatMemoryGcBo.getJvmGcOldTime());
        agentStat.setHeapUsed(agentStatMemoryGcBo.getJvmMemoryHeapUsed());
        agentStat.setHeapMax(agentStatMemoryGcBo.getJvmMemoryHeapMax());
        agentStat.setNonHeapUsed(agentStatMemoryGcBo.getJvmMemoryNonHeapUsed());
        agentStat.setNonHeapMax(agentStatMemoryGcBo.getJvmMemoryNonHeapMax());

        List<AgentStat> result = new ArrayList<>(1);
        result.add(agentStat);
        return result;
    }
}