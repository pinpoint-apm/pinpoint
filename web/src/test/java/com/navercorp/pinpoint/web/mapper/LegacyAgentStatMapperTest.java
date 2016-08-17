/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.AgentStatCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.AgentStatMemoryGcBo;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;
import com.navercorp.pinpoint.web.mapper.stat.LegacyAgentStatMapper;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author HyunGil Jeong
 */
public class LegacyAgentStatMapperTest {

    // for comparing CPU Usage up to 2 decimal places
    private static final double DELTA = 1e-4;

    private static final String AGENT_ID = "agentId";
    private static final long TIMESTAMP = System.currentTimeMillis();
    private static final byte[] ROW_KEY = RowKeyUtils.concatFixedByteAndLong(BytesUtils.toBytes(AGENT_ID), AGENT_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(TIMESTAMP));

    private static final long COLLECT_INTERVAL = 5000L;

    private static final TJvmGcType GC_TYPE = TJvmGcType.G1;
    private static final long GC_OLD_COUNT = 0L;
    private static final long GC_OLD_TIME = Long.MAX_VALUE;
    private static final long HEAP_USED = 1024L;
    private static final long HEAP_MAX = 4096L;
    private static final long NON_HEAP_USED = 52L;
    private static final long NON_HEAP_MAX = -1L;

    private static final double JVM_CPU_USAGE = 10;
    private static final double SYS_CPU_USAGE = 20;

    private static final long SAMPLED_NEW_COUNT = 100L;
    private static final long SAMPLED_CONTINUATION_COUNT = 200L;
    private static final long UNSAMPLED_NEW_COUNT = 50L;
    private static final long UNSAMPLED_CONTINUATION_COUNT = 150L;

    @Mock
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @InjectMocks
    private RowMapper<List<AgentStat>> mapper = new LegacyAgentStatMapper();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.rowKeyDistributorByHashPrefix.getOriginalKey(any(byte[].class))).thenReturn(ROW_KEY);
    }

    @Test
    public void test_legacy_column_storage() throws Exception {
        // Given
        final Result result = Result.create(Arrays.asList(
                createCell(AGENT_STAT_COL_INTERVAL, Bytes.toBytes(COLLECT_INTERVAL)),
                createCell(AGENT_STAT_COL_GC_TYPE, Bytes.toBytes(GC_TYPE.name())),
                createCell(AGENT_STAT_COL_GC_OLD_COUNT, Bytes.toBytes(GC_OLD_COUNT)),
                createCell(AGENT_STAT_COL_GC_OLD_TIME, Bytes.toBytes(GC_OLD_TIME)),
                createCell(AGENT_STAT_COL_HEAP_USED, Bytes.toBytes(HEAP_USED)),
                createCell(AGENT_STAT_COL_HEAP_MAX, Bytes.toBytes(HEAP_MAX)),
                createCell(AGENT_STAT_COL_NON_HEAP_USED, Bytes.toBytes(NON_HEAP_USED)),
                createCell(AGENT_STAT_COL_NON_HEAP_MAX, Bytes.toBytes(NON_HEAP_MAX)),
                createCell(AGENT_STAT_COL_JVM_CPU, Bytes.toBytes(JVM_CPU_USAGE)),
                createCell(AGENT_STAT_COL_SYS_CPU, Bytes.toBytes(SYS_CPU_USAGE)),
                createCell(AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW, Bytes.toBytes(SAMPLED_NEW_COUNT)),
                createCell(AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION, Bytes.toBytes(SAMPLED_CONTINUATION_COUNT)),
                createCell(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW, Bytes.toBytes(UNSAMPLED_NEW_COUNT)),
                createCell(AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION, Bytes.toBytes(UNSAMPLED_CONTINUATION_COUNT))
                ));
        // When
        List<AgentStat> agentStats = this.mapper.mapRow(result, 0);
        // Then
        assertNotNull(agentStats);
        assertThat(agentStats.size(), is(1));
        AgentStat agentStat = agentStats.get(0);

        assertEquals(COLLECT_INTERVAL, agentStat.getCollectInterval());
        assertJvmGc(agentStat);
        assertCpuUsage(agentStat);
        assertTransaction(agentStat);
    }

    @Test
    public void test_legacy_with_AGENT_STAT_CF_STATISTICS_V1() throws Exception {
        // Given
        final Result result = createResultForLegacyWith_AGENT_STAT_CF_STATISTICS_V1();
        // When
        List<AgentStat> agentStats = this.mapper.mapRow(result, 0);
        // Then
        assertNotNull(agentStats);
        assertThat(agentStats.size(), is(1));
        AgentStat agentStat = agentStats.get(0);

        assertEquals(0, agentStat.getCollectInterval());
        assertJvmGc(agentStat);
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getJvmCpuUsage(), DELTA);
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getSystemCpuUsage(), DELTA);
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getSampledNewCount());
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getSampledContinuationCount());
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getUnsampledNewCount());
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getUnsampledContinuationCount());
    }

    @Test
    public void test_legacy_serialized_BOs() throws Exception {
        // Given
        final Result result = createResultForLegacy_serialized_BOs();
        // When
        List<AgentStat> agentStats = this.mapper.mapRow(result, 0);
        // Then
        assertNotNull(agentStats);
        assertThat(agentStats.size(), is(1));
        AgentStat agentStat = agentStats.get(0);

        assertEquals(0, agentStat.getCollectInterval());
        assertJvmGc(agentStat);
        assertCpuUsage(agentStat);
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getSampledNewCount());
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getSampledContinuationCount());
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getUnsampledNewCount());
        assertEquals(AgentStat.NOT_COLLECTED, agentStat.getUnsampledContinuationCount());
    }

    private void assertJvmGc(AgentStat agentStat) {
        assertEquals(AGENT_ID, agentStat.getAgentId());
        assertEquals(TIMESTAMP, agentStat.getTimestamp());
        assertEquals(GC_TYPE.name(), agentStat.getGcType());
        assertEquals(GC_OLD_COUNT, agentStat.getGcOldCount());
        assertEquals(GC_OLD_TIME, agentStat.getGcOldTime());
        assertEquals(HEAP_USED, agentStat.getHeapUsed());
        assertEquals(HEAP_MAX, agentStat.getHeapMax());
        assertEquals(NON_HEAP_USED, agentStat.getNonHeapUsed());
        assertEquals(NON_HEAP_MAX, agentStat.getNonHeapMax());
    }

    private void assertCpuUsage(AgentStat agentStat) {
        assertEquals(JVM_CPU_USAGE, agentStat.getJvmCpuUsage(), DELTA);
        assertEquals(SYS_CPU_USAGE, agentStat.getSystemCpuUsage(), DELTA);
    }

    private void assertTransaction(AgentStat agentStat) {
        assertEquals(SAMPLED_NEW_COUNT, agentStat.getSampledNewCount());
        assertEquals(SAMPLED_CONTINUATION_COUNT, agentStat.getSampledContinuationCount());
        assertEquals(UNSAMPLED_NEW_COUNT, agentStat.getUnsampledNewCount());
        assertEquals(UNSAMPLED_CONTINUATION_COUNT, agentStat.getUnsampledContinuationCount());
    }

    private Result createResultForLegacyWith_AGENT_STAT_CF_STATISTICS_V1() throws TException {
        final TAgentStat agentStat = new TAgentStat();
        final TJvmGc gc = new TJvmGc();
        gc.setType(GC_TYPE);
        gc.setJvmGcOldCount(GC_OLD_COUNT);
        gc.setJvmGcOldTime(GC_OLD_TIME);
        gc.setJvmMemoryHeapUsed(HEAP_USED);
        gc.setJvmMemoryHeapMax(HEAP_MAX);
        gc.setJvmMemoryNonHeapUsed(NON_HEAP_USED);
        gc.setJvmMemoryNonHeapMax(NON_HEAP_MAX);
        agentStat.setGc(gc);

        final TProtocolFactory factory = new TCompactProtocol.Factory();
        final TSerializer serializer = new TSerializer(factory);
        final byte[] qualifier = AGENT_STAT_CF_STATISTICS_V1;
        final byte[] value = serializer.serialize(agentStat);
        return Result.create(Arrays.asList(createCell(qualifier, value)));
    }

    private Result createResultForLegacy_serialized_BOs() {
        final AgentStatMemoryGcBo.Builder jvmGcBuilder = new AgentStatMemoryGcBo.Builder(AGENT_ID, 0L, TIMESTAMP);
        jvmGcBuilder.gcType(GC_TYPE.name());
        jvmGcBuilder.jvmGcOldCount(GC_OLD_COUNT);
        jvmGcBuilder.jvmGcOldTime(GC_OLD_TIME);
        jvmGcBuilder.jvmMemoryHeapUsed(HEAP_USED);
        jvmGcBuilder.jvmMemoryHeapMax(HEAP_MAX);
        jvmGcBuilder.jvmMemoryNonHeapUsed(NON_HEAP_USED);
        jvmGcBuilder.jvmMemoryNonHeapMax(NON_HEAP_MAX);
        final AgentStatCpuLoadBo.Builder cpuLoadBuilder = new AgentStatCpuLoadBo.Builder(AGENT_ID, 0L, TIMESTAMP);
        cpuLoadBuilder.jvmCpuLoad(JVM_CPU_USAGE);
        cpuLoadBuilder.systemCpuLoad(SYS_CPU_USAGE);
        final AgentStatMemoryGcBo jvmGc = jvmGcBuilder.build();
        final AgentStatCpuLoadBo cpuLoad = cpuLoadBuilder.build();

        final Cell jvmGcCell = createCell(AGENT_STAT_CF_STATISTICS_MEMORY_GC, jvmGc.writeValue());
        final Cell cpuLoadCell = createCell(AGENT_STAT_CF_STATISTICS_CPU_LOAD, cpuLoad.writeValue());

        return Result.create(Arrays.asList(jvmGcCell, cpuLoadCell));

    }

    private Cell createCell(byte[] qualifier, byte[] value) {
        return CellUtil.createCell(ROW_KEY, AGENT_STAT_CF_STATISTICS, qualifier, HConstants.LATEST_TIMESTAMP,
                KeyValue.Type.Maximum.getCode(), value);
    }

}
