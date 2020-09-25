/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTrace;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTraceHistogram;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSource;
import com.navercorp.pinpoint.thrift.dto.flink.TFJvmGc;
import com.navercorp.pinpoint.thrift.dto.flink.TFResponseTime;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author minwoo.jung
 */
public class TFAgentStatMapperTest {

    public static final String TEST_AGENT = "test_agent";
    public static final long startTimestamp = 1496370596375L;
    public static final long collectTime1st = startTimestamp + 5000;
    public static final long collectTime2nd = collectTime1st + 5000;
    public static final long collectTime3rd = collectTime2nd + 5000;

    private final FlinkStatMapper<?, ?>[] statMappers = {
            new TFActiveTraceMapper(),
            new TFCpuLoadMapper(),
            new TFDataSourceListBoMapper(),
            new TFDirectBufferMapper(),
            new TFFileDescriptorMapper(),
            new TFJvmGcMapper(),
            new TFLoadedClassMapper(),
            new TFResponseTimeMapper(),
            new TFTotalThreadCountMapper(),
            new TFTransactionMapper(),
    };

    private TFAgentStatMapper newAgentStatMapper() {
        return new TFAgentStatMapper(statMappers);
    }

    @Test
    public void mapTest() {
        AgentStatBo agentStatBo = createCpuLoadBoList();

        List<TFAgentStat> tFAgentStatList = newAgentStatMapper().map(agentStatBo);
        assertEquals(3, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(TEST_AGENT, tFAgentStat1.getAgentId());
        assertEquals(startTimestamp, tFAgentStat1.getStartTimestamp());
        assertEquals(collectTime1st, tFAgentStat1.getTimestamp());
        assertEquals(4, tFAgentStat1.getCpuLoad().getJvmCpuLoad(), 0);
        assertEquals(3, tFAgentStat1.getCpuLoad().getSystemCpuLoad(), 0);

        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(TEST_AGENT, tFAgentStat2.getAgentId());
        assertEquals(startTimestamp, tFAgentStat2.getStartTimestamp());
        assertEquals(collectTime2nd, tFAgentStat2.getTimestamp());
        assertEquals(5, tFAgentStat2.getCpuLoad().getJvmCpuLoad(), 0);
        assertEquals(6, tFAgentStat2.getCpuLoad().getSystemCpuLoad(), 0);

        TFAgentStat tFAgentStat3 = tFAgentStatList.get(2);
        assertEquals(TEST_AGENT, tFAgentStat3.getAgentId());
        assertEquals(startTimestamp, tFAgentStat3.getStartTimestamp());
        assertEquals(collectTime3rd, tFAgentStat3.getTimestamp());
        assertEquals(8, tFAgentStat3.getCpuLoad().getJvmCpuLoad(), 0);
        assertEquals(9, tFAgentStat3.getCpuLoad().getSystemCpuLoad(), 0);
    }



    private AgentStatBo createCpuLoadBoList() {
        AgentStatBo.Builder builder = newBuilder();

        CpuLoadBo cpuLoadBo1 = new CpuLoadBo();
        cpuLoadBo1.setJvmCpuLoad(4);
        cpuLoadBo1.setSystemCpuLoad(3);
        AgentStatBo.Builder.StatBuilder statBuilder1 = builder.newStatBuilder(collectTime1st);
        statBuilder1.addCpuLoad(cpuLoadBo1);

        CpuLoadBo cpuLoadBo3 = new CpuLoadBo();
        cpuLoadBo3.setJvmCpuLoad(8);
        cpuLoadBo3.setSystemCpuLoad(9);
        AgentStatBo.Builder.StatBuilder statBuilder3 = builder.newStatBuilder(collectTime3rd);
        statBuilder3.addCpuLoad(cpuLoadBo3);

        CpuLoadBo cpuLoadBo2 = new CpuLoadBo();
        cpuLoadBo2.setJvmCpuLoad(5);
        cpuLoadBo2.setSystemCpuLoad(6);

        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addCpuLoad(cpuLoadBo2);

        return builder.build();
    }

    @Test
    public void map2Test() {
        AgentStatBo agentStatBo = createDataSourceListBoList();

        List<TFAgentStat> tFAgentStatList = newAgentStatMapper().map(agentStatBo);
        assertEquals(2, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(tFAgentStat1.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat1.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat1.getTimestamp(), collectTime1st);
        assertTrue(tFAgentStat1.isSetDataSourceList());
        List<TFDataSource> dataSourceList1 = tFAgentStat1.getDataSourceList().getDataSourceList();
        assertEquals(dataSourceList1.size(), 2);
        TFDataSource tfDataSource1_1 = dataSourceList1.get(0);
        TFDataSource tfDataSource1_2 = dataSourceList1.get(1);
        assertEquals(tfDataSource1_1.getId(), 1);
        assertEquals(tfDataSource1_1.getUrl(), "jdbc:mysql");
        assertEquals(tfDataSource1_1.getServiceTypeCode(), 1000);
        assertEquals(tfDataSource1_1.getActiveConnectionSize(), 15);
        assertEquals(tfDataSource1_1.getMaxConnectionSize(), 30);
        assertEquals(tfDataSource1_1.getDatabaseName(), "pinpoint1");
        assertEquals(tfDataSource1_2.getId(), 2);
        assertEquals(tfDataSource1_2.getUrl(), "jdbc:mssql");
        assertEquals(tfDataSource1_2.getServiceTypeCode(), 2000);
        assertEquals(tfDataSource1_2.getActiveConnectionSize(), 25);
        assertEquals(tfDataSource1_2.getMaxConnectionSize(), 40);
        assertEquals(tfDataSource1_2.getDatabaseName(), "pinpoint2");


        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(tFAgentStat2.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat2.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat2.getTimestamp(), collectTime2nd);
        assertTrue(tFAgentStat2.isSetDataSourceList());
        List<TFDataSource> dataSourceList2 = tFAgentStat2.getDataSourceList().getDataSourceList();
        assertEquals(dataSourceList2.size(), 2);
        TFDataSource tfDataSource2_1 = dataSourceList2.get(0);
        TFDataSource tfDataSource2_2 = dataSourceList2.get(1);
        assertEquals(tfDataSource2_1.getId(), 1);
        assertEquals(tfDataSource2_1.getUrl(), "jdbc:mysql");
        assertEquals(tfDataSource2_1.getServiceTypeCode(), 1000);
        assertEquals(tfDataSource2_1.getActiveConnectionSize(), 16);
        assertEquals(tfDataSource2_1.getMaxConnectionSize(), 31);
        assertEquals(tfDataSource2_1.getDatabaseName(), "pinpoint1");
        assertEquals(tfDataSource2_2.getId(), 2);
        assertEquals(tfDataSource2_2.getUrl(), "jdbc:mssql");
        assertEquals(tfDataSource2_2.getServiceTypeCode(), 2000);
        assertEquals(tfDataSource2_2.getActiveConnectionSize(), 26);
        assertEquals(tfDataSource2_2.getMaxConnectionSize(), 41);
        assertEquals(tfDataSource2_2.getDatabaseName(), "pinpoint2");
    }

    private AgentStatBo createDataSourceListBoList() {
        AgentStatBo.Builder builder = AgentStatBo.newBuilder(TEST_AGENT, startTimestamp);

        DataSourceListBo dataSourceListBo1 = new DataSourceListBo();
        DataSourceBo dataSourceBo1_1 = new DataSourceBo();
        dataSourceBo1_1.setServiceTypeCode((short) 1000);
        dataSourceBo1_1.setJdbcUrl("jdbc:mysql");
        dataSourceBo1_1.setActiveConnectionSize(15);
        dataSourceBo1_1.setMaxConnectionSize(30);
        dataSourceBo1_1.setId(1);
        dataSourceBo1_1.setDatabaseName("pinpoint1");

        DataSourceBo dataSourceBo1_2 = new DataSourceBo();
        dataSourceBo1_2.setServiceTypeCode((short) 2000);
        dataSourceBo1_2.setJdbcUrl("jdbc:mssql");
        dataSourceBo1_2.setActiveConnectionSize(25);
        dataSourceBo1_2.setMaxConnectionSize(40);
        dataSourceBo1_2.setId(2);
        dataSourceBo1_2.setDatabaseName("pinpoint2");
        dataSourceListBo1.add(dataSourceBo1_1);
        dataSourceListBo1.add(dataSourceBo1_2);

        DataSourceListBo dataSourceListBo2 = new DataSourceListBo();
        DataSourceBo dataSourceBo2_1 = new DataSourceBo();
        dataSourceBo2_1.setServiceTypeCode((short) 1000);
        dataSourceBo2_1.setJdbcUrl("jdbc:mysql");
        dataSourceBo2_1.setActiveConnectionSize(16);
        dataSourceBo2_1.setMaxConnectionSize(31);
        dataSourceBo2_1.setId(1);
        dataSourceBo2_1.setDatabaseName("pinpoint1");
        DataSourceBo dataSourceBo2_2 = new DataSourceBo();
        dataSourceBo2_2.setServiceTypeCode((short) 2000);
        dataSourceBo2_2.setJdbcUrl("jdbc:mssql");
        dataSourceBo2_2.setActiveConnectionSize(26);
        dataSourceBo2_2.setMaxConnectionSize(41);
        dataSourceBo2_2.setId(2);
        dataSourceBo2_2.setDatabaseName("pinpoint2");
        dataSourceListBo2.add(dataSourceBo2_1);
        dataSourceListBo2.add(dataSourceBo2_2);

        AgentStatBo.Builder.StatBuilder statBuilder1 = builder.newStatBuilder(collectTime1st);
        statBuilder1.addDataSourceList(dataSourceListBo1);
        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addDataSourceList(dataSourceListBo2);

        return builder.build();
    }

    @Test
    public void map3Test() throws Exception {
        AgentStatBo agentStatBo = createJvmGcBoList();

        List<TFAgentStat> tFAgentStatList = newAgentStatMapper().map(agentStatBo);
        assertEquals(2, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(tFAgentStat1.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat1.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat1.getTimestamp(), collectTime1st);
        TFJvmGc tFJvmGc1 = tFAgentStat1.getGc();
        assertEquals(tFJvmGc1.getJvmMemoryHeapUsed(), 3000);
        assertEquals(tFJvmGc1.getJvmMemoryNonHeapUsed(), 300);

        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(tFAgentStat2.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat2.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat2.getTimestamp(), collectTime2nd);
        TFJvmGc tFJvmGc2 = tFAgentStat2.getGc();
        assertEquals(tFJvmGc2.getJvmMemoryHeapUsed(), 3100);
        assertEquals(tFJvmGc2.getJvmMemoryNonHeapUsed(), 310);
    }

    private AgentStatBo createJvmGcBoList() {
        AgentStatBo.Builder builder = newBuilder();

        JvmGcBo jvmGcBo1 = new JvmGcBo();
        jvmGcBo1.setGcType(JvmGcType.G1);
        jvmGcBo1.setHeapUsed(3000);
        jvmGcBo1.setHeapMax(5000);
        jvmGcBo1.setNonHeapUsed(300);
        jvmGcBo1.setNonHeapMax(500);
        jvmGcBo1.setGcOldCount(5);
        jvmGcBo1.setGcOldTime(10);
        AgentStatBo.Builder.StatBuilder statBuilder1 = builder.newStatBuilder(collectTime1st);
        statBuilder1.addJvmGc(jvmGcBo1);

        JvmGcBo jvmGcBo2 = new JvmGcBo();
        jvmGcBo2.setGcType(JvmGcType.G1);
        jvmGcBo2.setHeapUsed(3100);
        jvmGcBo2.setHeapMax(5100);
        jvmGcBo2.setNonHeapUsed(310);
        jvmGcBo2.setNonHeapMax(510);
        jvmGcBo2.setGcOldCount(15);
        jvmGcBo2.setGcOldTime(20);
        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addJvmGc(jvmGcBo2);

        return builder.build();
    }

    @Test
    public void map4Test() {
        AgentStatBo agentStatBo = createActiveTraceBoList();


        List<TFAgentStat> tFAgentStatList = newAgentStatMapper().map(agentStatBo);
        assertEquals(2, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(tFAgentStat1.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat1.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat1.getTimestamp(), collectTime1st);
        TFActiveTrace activeTrace1 = tFAgentStat1.getActiveTrace();
        TFActiveTraceHistogram histogram1 = activeTrace1.getHistogram();
        List<Integer> activeTraceCount1 = histogram1.getActiveTraceCount();
        assertEquals((int) activeTraceCount1.get(0), 30);
        assertEquals((int) activeTraceCount1.get(1), 40);
        assertEquals((int) activeTraceCount1.get(2), 10);
        assertEquals((int) activeTraceCount1.get(3), 50);

        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(tFAgentStat2.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat2.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat2.getTimestamp(), collectTime2nd);
        TFActiveTrace activeTrace2 = tFAgentStat2.getActiveTrace();
        TFActiveTraceHistogram histogram2 = activeTrace2.getHistogram();
        List<Integer> activeTraceCount2 = histogram2.getActiveTraceCount();
        assertEquals((int) activeTraceCount2.get(0), 31);
        assertEquals((int) activeTraceCount2.get(1), 41);
        assertEquals((int) activeTraceCount2.get(2), 11);
        assertEquals((int) activeTraceCount2.get(3), 51);
    }

    private AgentStatBo createActiveTraceBoList() {
        AgentStatBo.Builder builder = newBuilder();

        ActiveTraceBo activeTraceBo1 = new ActiveTraceBo();

        activeTraceBo1.setVersion((short) 1);
        activeTraceBo1.setHistogramSchemaType(2);
        ActiveTraceHistogram activeTraceHistogram1 = new ActiveTraceHistogram(30, 40, 10, 50);
        activeTraceBo1.setActiveTraceHistogram(activeTraceHistogram1);
        AgentStatBo.Builder.StatBuilder statBuilder1 = builder.newStatBuilder(collectTime1st);
        statBuilder1.addActiveTrace(activeTraceBo1);

        ActiveTraceBo activeTraceBo2 = new ActiveTraceBo();
        activeTraceBo2.setTimestamp(collectTime2nd);
        activeTraceBo2.setVersion((short) 1);
        activeTraceBo2.setHistogramSchemaType(2);

        ActiveTraceHistogram activeTraceHistogram2 = new ActiveTraceHistogram(31, 41, 11, 51);
        activeTraceBo2.setActiveTraceHistogram(activeTraceHistogram2);

        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addActiveTrace(activeTraceBo2);

        return builder.build();
    }

    @Test
    public void map5Test() {

        AgentStatBo agentStatBo = createResponseTimeBoList();

        List<TFAgentStat> tFAgentStatList = newAgentStatMapper().map(agentStatBo);
        assertEquals(2, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(tFAgentStat1.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat1.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat1.getTimestamp(), collectTime1st);
        TFResponseTime responseTime1 = tFAgentStat1.getResponseTime();
        assertEquals(responseTime1.getAvg(), 1000);

        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(tFAgentStat2.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat2.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat2.getTimestamp(), collectTime2nd);
        TFResponseTime responseTime2 = tFAgentStat2.getResponseTime();
        assertEquals(responseTime2.getAvg(), 2000);
    }

    private AgentStatBo createResponseTimeBoList() {
        AgentStatBo.Builder builder = newBuilder();

        ResponseTimeBo responseTimeBo1 = new ResponseTimeBo();
        responseTimeBo1.setAvg(1000);
        AgentStatBo.Builder.StatBuilder statBuilder1 = builder.newStatBuilder(collectTime1st);
        statBuilder1.addResponseTime(responseTimeBo1);

        ResponseTimeBo responseTimeBo2 = new ResponseTimeBo();
        responseTimeBo2.setAvg(2000);
        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addResponseTime(responseTimeBo2);

        return builder.build();
    }

    @Test
    public void map6Test() throws Exception {

        AgentStatBo agentStatBo = createFileDescriptorBoList();

        List<TFAgentStat> tFAgentStatList = newAgentStatMapper().map(agentStatBo);
        assertEquals(3, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(TEST_AGENT, tFAgentStat1.getAgentId());
        assertEquals(startTimestamp, tFAgentStat1.getStartTimestamp());
        assertEquals(collectTime1st, tFAgentStat1.getTimestamp());
        assertEquals(4, tFAgentStat1.getFileDescriptor().getOpenFileDescriptorCount(), 0);

        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(TEST_AGENT, tFAgentStat2.getAgentId());
        assertEquals(startTimestamp, tFAgentStat2.getStartTimestamp());
        assertEquals(collectTime2nd, tFAgentStat2.getTimestamp());
        assertEquals(5, tFAgentStat2.getFileDescriptor().getOpenFileDescriptorCount(), 0);

        TFAgentStat tFAgentStat3 = tFAgentStatList.get(2);
        assertEquals(TEST_AGENT, tFAgentStat3.getAgentId());
        assertEquals(startTimestamp, tFAgentStat3.getStartTimestamp());
        assertEquals(collectTime3rd, tFAgentStat3.getTimestamp());
        assertEquals(8, tFAgentStat3.getFileDescriptor().getOpenFileDescriptorCount(), 0);
    }

    private AgentStatBo createFileDescriptorBoList() {
        final AgentStatBo.Builder builder = newBuilder();

        FileDescriptorBo fileDescriptorBo1 = new FileDescriptorBo();
        fileDescriptorBo1.setOpenFileDescriptorCount(4);
        AgentStatBo.Builder.StatBuilder statBuilder1 = builder.newStatBuilder(collectTime1st);
        statBuilder1.addFileDescriptor(fileDescriptorBo1);


        FileDescriptorBo fileDescriptorBo2 = new FileDescriptorBo();
        fileDescriptorBo2.setOpenFileDescriptorCount(5);
        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addFileDescriptor(fileDescriptorBo2);

        FileDescriptorBo fileDescriptorBo3 = new FileDescriptorBo();
        fileDescriptorBo3.setOpenFileDescriptorCount(8);
        AgentStatBo.Builder.StatBuilder statBuilder3 = builder.newStatBuilder(collectTime3rd);
        statBuilder3.addFileDescriptor(fileDescriptorBo3);

        return builder.build();
    }

    private AgentStatBo.Builder newBuilder() {
        return AgentStatBo.newBuilder(TEST_AGENT, startTimestamp);
    }

    @Test
    public void map7Test() {
        AgentStatBo agentStatBo = createTotalThreadCountBoList();

        List<TFAgentStat> tFAgentStatList = newAgentStatMapper().map(agentStatBo);
        assertEquals(3, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(TEST_AGENT, tFAgentStat1.getAgentId());
        assertEquals(startTimestamp, tFAgentStat1.getStartTimestamp());
        assertEquals(collectTime1st, tFAgentStat1.getTimestamp());
        assertEquals(4, tFAgentStat1.getTotalThreadCount().getTotalThreadCount(), 0);

        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(TEST_AGENT, tFAgentStat2.getAgentId());
        assertEquals(startTimestamp, tFAgentStat2.getStartTimestamp());
        assertEquals(collectTime2nd, tFAgentStat2.getTimestamp());
        assertEquals(5, tFAgentStat2.getTotalThreadCount().getTotalThreadCount(), 0);

        TFAgentStat tFAgentStat3 = tFAgentStatList.get(2);
        assertEquals(TEST_AGENT, tFAgentStat3.getAgentId());
        assertEquals(startTimestamp, tFAgentStat3.getStartTimestamp());
        assertEquals(collectTime3rd, tFAgentStat3.getTimestamp());
        assertEquals(8, tFAgentStat3.getTotalThreadCount().getTotalThreadCount(), 0);
    }

    private AgentStatBo createTotalThreadCountBoList() {
        AgentStatBo.Builder builder = newBuilder();

        TotalThreadCountBo totalThreadCountBo1 = new TotalThreadCountBo();
        totalThreadCountBo1.setAgentId(TEST_AGENT);
        totalThreadCountBo1.setTimestamp(collectTime1st);
        totalThreadCountBo1.setStartTimestamp(startTimestamp);
        totalThreadCountBo1.setTotalThreadCount(4);
        AgentStatBo.Builder.StatBuilder statBuilder1 = builder.newStatBuilder(collectTime1st);
        statBuilder1.addTotalThreadCount(totalThreadCountBo1);

        TotalThreadCountBo totalThreadCountBo2 = new TotalThreadCountBo();
        totalThreadCountBo2.setTotalThreadCount(5);
        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addTotalThreadCount(totalThreadCountBo2);

        TotalThreadCountBo totalThreadCountBo3 = new TotalThreadCountBo();
        totalThreadCountBo3.setTotalThreadCount(8);
        AgentStatBo.Builder.StatBuilder statBuilder3 = builder.newStatBuilder(collectTime3rd);
        statBuilder3.addTotalThreadCount(totalThreadCountBo3);
        return builder.build();
    }

    @Test
    public void map8Test() {
        AgentStatBo agentStatBo = createLoadedClassCountBoList();

        List<TFAgentStat> tFAgentStatList = newAgentStatMapper().map(agentStatBo);
        assertEquals(3, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(TEST_AGENT, tFAgentStat1.getAgentId());
        assertEquals(startTimestamp, tFAgentStat1.getStartTimestamp());
        assertEquals(collectTime1st, tFAgentStat1.getTimestamp());
        assertEquals(4, tFAgentStat1.getLoadedClass().getLoadedClassCount(), 0);
        assertEquals(4, tFAgentStat1.getLoadedClass().getUnloadedClassCount(), 0);

        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(TEST_AGENT, tFAgentStat2.getAgentId());
        assertEquals(startTimestamp, tFAgentStat2.getStartTimestamp());
        assertEquals(collectTime2nd, tFAgentStat2.getTimestamp());
        assertEquals(5, tFAgentStat2.getLoadedClass().getLoadedClassCount(), 0);
        assertEquals(5, tFAgentStat2.getLoadedClass().getUnloadedClassCount(), 0);

        TFAgentStat tFAgentStat3 = tFAgentStatList.get(2);
        assertEquals(TEST_AGENT, tFAgentStat3.getAgentId());
        assertEquals(startTimestamp, tFAgentStat3.getStartTimestamp());
        assertEquals(collectTime3rd, tFAgentStat3.getTimestamp());
        assertEquals(6, tFAgentStat3.getLoadedClass().getLoadedClassCount(), 0);
        assertEquals(6, tFAgentStat3.getLoadedClass().getUnloadedClassCount(), 0);
    }

    private AgentStatBo createLoadedClassCountBoList() {
        AgentStatBo.Builder builder = newBuilder();

        LoadedClassBo loadedClassBo1 = new LoadedClassBo();
        loadedClassBo1.setLoadedClassCount(4);
        loadedClassBo1.setUnloadedClassCount(4);
        AgentStatBo.Builder.StatBuilder statBuilder1 = builder.newStatBuilder(collectTime1st);
        statBuilder1.addLoadedClass(loadedClassBo1);

        LoadedClassBo loadedClassBo2 = new LoadedClassBo();
        loadedClassBo2.setLoadedClassCount(5);
        loadedClassBo2.setUnloadedClassCount(5);
        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addLoadedClass(loadedClassBo2);

        LoadedClassBo loadedClassBo3 = new LoadedClassBo();
        loadedClassBo3.setLoadedClassCount(6);
        loadedClassBo3.setUnloadedClassCount(6);
        AgentStatBo.Builder.StatBuilder statBuilder3 = builder.newStatBuilder(collectTime3rd);
        statBuilder3.addLoadedClass(loadedClassBo3);

        return builder.build();
    }
}