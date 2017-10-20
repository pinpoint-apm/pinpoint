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

import com.navercorp.pinpoint.common.server.bo.ActiveTraceHistogramBo;
import com.navercorp.pinpoint.common.server.bo.AgentStatCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.AgentStatMemoryGcBo;
import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

/**
 * @author HyunGil Jeong
 */
@Deprecated
public abstract class AgentStatMapperV1<T extends AgentStatDataPoint> implements AgentStatMapper<T> {

    private static final TProtocolFactory FACTORY = new TCompactProtocol.Factory();

    @Autowired
    @Qualifier("agentStatRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @Override
    public List<T> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());
        final String agentId = BytesUtils.toStringAndRightTrim(rowKey, 0, AGENT_NAME_MAX_LEN);
        final long reverseTimestamp = BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN);
        final long timestamp = TimeUtils.recoveryTimeMillis(reverseTimestamp);
        NavigableMap<byte[], byte[]> qualifierMap = result.getFamilyMap(AGENT_STAT_CF_STATISTICS);

        if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_V1)) {
            // FIXME (2014.08) Legacy support for TAgentStat Thrift DTO stored directly into hbase.
            return readAgentStatThriftDto(agentId, timestamp, qualifierMap.get(AGENT_STAT_CF_STATISTICS_V1));
        } else if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_MEMORY_GC) || qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_CPU_LOAD)) {
            // FIXME (2015.10) Legacy column for storing serialzied Bos separately.
            return readSerializedBos(agentId, timestamp, qualifierMap);
        } else {
            return mapQualifiers(agentId, timestamp, qualifierMap);
        }
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }

    // FIXME (2014.08) Legacy support for TAgentStat Thrift DTO stored directly into hbase.
    @Deprecated
    protected abstract List<T> readAgentStatThriftDto(String agentId, long timestamp, byte[]tAgentStatByteArray) throws TException;

    // FIXME (2015.10) Legacy column for storing serialzied Bos separately.
    @Deprecated
    protected abstract List<T> readSerializedBos(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap);

    protected abstract List<T> mapQualifiers(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap);

    @Deprecated
    @Component("jvmGcMapper")
    public static class JvmGcMapper extends AgentStatMapperV1<JvmGcBo> {

        @Override
        protected List<JvmGcBo> readAgentStatThriftDto(String agentId, long timestamp, byte[] tAgentStatByteArray) throws TException {
            // CompactProtocol used
            TDeserializer deserializer = new TDeserializer(FACTORY);
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
            AgentStatMemoryGcBo agentStatMemoryGcBo = memoryGcBoBuilder.build();
            JvmGcBo jvmGcBo = new JvmGcBo();
            jvmGcBo.setAgentId(agentStatMemoryGcBo.getAgentId());
            jvmGcBo.setTimestamp(agentStatMemoryGcBo.getTimestamp());
            jvmGcBo.setGcType(JvmGcType.valueOf(agentStatMemoryGcBo.getGcType()));
            jvmGcBo.setGcOldCount(agentStatMemoryGcBo.getJvmGcOldCount());
            jvmGcBo.setGcOldTime(agentStatMemoryGcBo.getJvmGcOldTime());
            jvmGcBo.setHeapUsed(agentStatMemoryGcBo.getJvmMemoryHeapUsed());
            jvmGcBo.setHeapMax(agentStatMemoryGcBo.getJvmMemoryHeapMax());
            jvmGcBo.setNonHeapUsed(agentStatMemoryGcBo.getJvmMemoryNonHeapUsed());
            jvmGcBo.setNonHeapMax(agentStatMemoryGcBo.getJvmMemoryNonHeapMax());
            return Arrays.asList(jvmGcBo);
        }

        @Override
        protected List<JvmGcBo> readSerializedBos(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_MEMORY_GC)) {
                AgentStatMemoryGcBo.Builder builder = new AgentStatMemoryGcBo.Builder(qualifierMap.get(AGENT_STAT_CF_STATISTICS_MEMORY_GC));
                AgentStatMemoryGcBo agentStatMemoryGcBo = builder.build();
                JvmGcBo jvmGcBo = new JvmGcBo();
                jvmGcBo.setAgentId(agentStatMemoryGcBo.getAgentId());
                jvmGcBo.setTimestamp(agentStatMemoryGcBo.getTimestamp());
                jvmGcBo.setGcType(JvmGcType.valueOf(agentStatMemoryGcBo.getGcType()));
                jvmGcBo.setGcOldCount(agentStatMemoryGcBo.getJvmGcOldCount());
                jvmGcBo.setGcOldTime(agentStatMemoryGcBo.getJvmGcOldTime());
                jvmGcBo.setHeapUsed(agentStatMemoryGcBo.getJvmMemoryHeapUsed());
                jvmGcBo.setHeapMax(agentStatMemoryGcBo.getJvmMemoryHeapMax());
                jvmGcBo.setNonHeapUsed(agentStatMemoryGcBo.getJvmMemoryNonHeapUsed());
                jvmGcBo.setNonHeapMax(agentStatMemoryGcBo.getJvmMemoryNonHeapMax());
                return Arrays.asList(jvmGcBo);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        protected List<JvmGcBo> mapQualifiers(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            JvmGcBo jvmGcBo = new JvmGcBo();
            jvmGcBo.setAgentId(agentId);
            jvmGcBo.setTimestamp(timestamp);
            if (qualifierMap.containsKey(AGENT_STAT_COL_GC_TYPE)) {
                jvmGcBo.setGcType(JvmGcType.valueOf(Bytes.toString(qualifierMap.get(AGENT_STAT_COL_GC_TYPE))));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_GC_OLD_COUNT)) {
                jvmGcBo.setGcOldCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_GC_OLD_COUNT)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_GC_OLD_TIME)) {
                jvmGcBo.setGcOldTime(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_GC_OLD_TIME)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_HEAP_USED)) {
                jvmGcBo.setHeapUsed(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_HEAP_USED)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_HEAP_MAX)) {
                jvmGcBo.setHeapMax(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_HEAP_MAX)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_NON_HEAP_USED)) {
                jvmGcBo.setNonHeapUsed(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_NON_HEAP_USED)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_NON_HEAP_MAX)) {
                jvmGcBo.setNonHeapMax(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_NON_HEAP_MAX)));
            }
            return Arrays.asList(jvmGcBo);
        }
    }

    @Deprecated
    @Component("cpuLoadMapper")
    public static class CpuLoadMapper extends AgentStatMapperV1<CpuLoadBo> {

        @Override
        protected List<CpuLoadBo> readAgentStatThriftDto(String agentId, long timestamp, byte[] tAgentStatByteArray) throws TException {
            // cpu load collection wasn't implemented for this
            return Collections.emptyList();
        }

        @Override
        protected List<CpuLoadBo> readSerializedBos(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            if (qualifierMap.containsKey(AGENT_STAT_CF_STATISTICS_CPU_LOAD)) {
                AgentStatCpuLoadBo.Builder builder = new AgentStatCpuLoadBo.Builder(qualifierMap.get(AGENT_STAT_CF_STATISTICS_CPU_LOAD));
                AgentStatCpuLoadBo agentStatCpuLoadBo = builder.build();
                CpuLoadBo cpuLoadBo = new CpuLoadBo();
                cpuLoadBo.setAgentId(agentStatCpuLoadBo.getAgentId());
                cpuLoadBo.setTimestamp(agentStatCpuLoadBo.getTimestamp());
                cpuLoadBo.setJvmCpuLoad(agentStatCpuLoadBo.getJvmCpuLoad());
                cpuLoadBo.setSystemCpuLoad(agentStatCpuLoadBo.getSystemCpuLoad());
                return Arrays.asList(cpuLoadBo);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        protected List<CpuLoadBo> mapQualifiers(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            CpuLoadBo cpuLoadBo = new CpuLoadBo();
            cpuLoadBo.setAgentId(agentId);
            cpuLoadBo.setTimestamp(timestamp);
            if (qualifierMap.containsKey(AGENT_STAT_COL_JVM_CPU)) {
                cpuLoadBo.setJvmCpuLoad(Bytes.toDouble(qualifierMap.get(AGENT_STAT_COL_JVM_CPU)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_SYS_CPU)) {
                cpuLoadBo.setSystemCpuLoad(Bytes.toDouble(qualifierMap.get(AGENT_STAT_COL_SYS_CPU)));
            }
            return Arrays.asList(cpuLoadBo);
        }
    }

    @Deprecated
    @Component("transactionMapper")
    public static class TransactionMapper extends AgentStatMapperV1<TransactionBo> {

        @Override
        protected List<TransactionBo> readAgentStatThriftDto(String agentId, long timestamp, byte[] tAgentStatByteArray) throws TException {
            // transaction collection wasn't implemented for this
            return Collections.emptyList();
        }

        @Override
        protected List<TransactionBo> readSerializedBos(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            // transaction collection wasn't implemented for this
            return Collections.emptyList();
        }

        @Override
        protected List<TransactionBo> mapQualifiers(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            TransactionBo transactionBo = new TransactionBo();
            transactionBo.setAgentId(agentId);
            transactionBo.setTimestamp(timestamp);
            if (qualifierMap.containsKey(AGENT_STAT_COL_INTERVAL)) {
                transactionBo.setCollectInterval(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_INTERVAL)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW)) {
                transactionBo.setSampledNewCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION)) {
                transactionBo.setSampledContinuationCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW)) {
                transactionBo.setUnsampledNewCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW)));
            }
            if (qualifierMap.containsKey(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION)) {
                transactionBo.setUnsampledContinuationCount(Bytes.toLong(qualifierMap.get(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION)));
            }
            return Arrays.asList(transactionBo);
        }
    }

    @Deprecated
    @Component("activeTraceMapper")
    public static class ActiveTraceMapper extends AgentStatMapperV1<ActiveTraceBo> {

        @Override
        protected List<ActiveTraceBo> readAgentStatThriftDto(String agentId, long timestamp, byte[] tAgentStatByteArray) throws TException {
            // active trace collection wasn't implemented for this
            return Collections.emptyList();
        }

        @Override
        protected List<ActiveTraceBo> readSerializedBos(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            // active trace collection wasn't implemented for this
            return Collections.emptyList();
        }

        @Override
        protected List<ActiveTraceBo> mapQualifiers(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            ActiveTraceBo activeTraceBo = new ActiveTraceBo();
            activeTraceBo.setAgentId(agentId);
            activeTraceBo.setTimestamp(timestamp);
            if (qualifierMap.containsKey(AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM)) {
                ActiveTraceHistogramBo activeTraceHistogramBo = new ActiveTraceHistogramBo(qualifierMap.get(AGENT_STAT_COL_ACTIVE_TRACE_HISTOGRAM));
                activeTraceBo.setHistogramSchemaType(activeTraceHistogramBo.getHistogramSchemaType());
                activeTraceBo.setActiveTraceCounts(activeTraceHistogramBo.getActiveTraceCountMap());
            }
            return Arrays.asList(activeTraceBo);
        }
    }

    // datasource not support v1
    @Deprecated
    @Component("dataSourceMapper")
    public static class DataSourceMapper extends AgentStatMapperV1<DataSourceListBo> {

        @Override
        protected List<DataSourceListBo> readAgentStatThriftDto(String agentId, long timestamp, byte[] tAgentStatByteArray) throws TException {
            return Collections.emptyList();
        }

        @Override
        protected List<DataSourceListBo> readSerializedBos(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            return Collections.emptyList();
        }

        @Override
        protected List<DataSourceListBo> mapQualifiers(String agentId, long timestamp, Map<byte[], byte[]> qualifierMap) {
            return Collections.emptyList();
        }

    }

}
