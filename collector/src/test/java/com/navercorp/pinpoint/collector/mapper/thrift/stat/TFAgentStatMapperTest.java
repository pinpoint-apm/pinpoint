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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.stat.*;
import com.navercorp.pinpoint.thrift.dto.flink.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class TFAgentStatMapperTest {

    public static final String TEST_AGENT = "test_agent";
    public static final long startTimestamp = 1496370596375L;
    public static final long collectTime1st = startTimestamp + 5000;
    public static final long collectTime2nd = collectTime1st + 5000;
    public static final long collectTime3rd = collectTime2nd + 5000;

    @Test
    public void mapTest() throws Exception {
        final AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setStartTimestamp(startTimestamp);
        agentStatBo.setAgentId(TEST_AGENT);
        agentStatBo.setCpuLoadBos(createCpuLoadBoList());

        List<TFAgentStat> tFAgentStatList = new TFAgentStatMapper().map(agentStatBo);
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

    private List<CpuLoadBo> createCpuLoadBoList() {
        final List<CpuLoadBo> cpuLoadBoList = new ArrayList<>();

        CpuLoadBo cpuLoadBo1 = new CpuLoadBo();
        cpuLoadBo1.setAgentId(TEST_AGENT);
        cpuLoadBo1.setTimestamp(collectTime1st);
        cpuLoadBo1.setStartTimestamp(startTimestamp);
        cpuLoadBo1.setJvmCpuLoad(4);
        cpuLoadBo1.setSystemCpuLoad(3);
        cpuLoadBoList.add(cpuLoadBo1);

        CpuLoadBo cpuLoadBo3 = new CpuLoadBo();
        cpuLoadBo3.setAgentId("test_agent");
        cpuLoadBo3.setTimestamp(collectTime3rd);
        cpuLoadBo3.setStartTimestamp(startTimestamp);
        cpuLoadBo3.setJvmCpuLoad(8);
        cpuLoadBo3.setSystemCpuLoad(9);
        cpuLoadBoList.add(cpuLoadBo3);

        CpuLoadBo cpuLoadBo2 = new CpuLoadBo();
        cpuLoadBo2.setAgentId("test_agent");
        cpuLoadBo2.setTimestamp(collectTime2nd);
        cpuLoadBo2.setStartTimestamp(startTimestamp);
        cpuLoadBo2.setJvmCpuLoad(5);
        cpuLoadBo2.setSystemCpuLoad(6);
        cpuLoadBoList.add(cpuLoadBo2);

        return cpuLoadBoList;
    }

    @Test
    public void map2Test() throws Exception {
        final AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setStartTimestamp(startTimestamp);
        agentStatBo.setAgentId(TEST_AGENT);
        agentStatBo.setDataSourceListBos(createDataSourceListBoList());

        List<TFAgentStat> tFAgentStatList = new TFAgentStatMapper().map(agentStatBo);
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

    private List<DataSourceListBo> createDataSourceListBoList() {
        List<DataSourceListBo> dataSourceListBoList = new ArrayList<>();

        DataSourceListBo dataSourceListBo1 = new DataSourceListBo();
        dataSourceListBo1.setAgentId("test_agent1");
        dataSourceListBo1.setStartTimestamp(startTimestamp);
        dataSourceListBo1.setTimestamp(collectTime1st);

        DataSourceBo dataSourceBo1_1 = new DataSourceBo();
        dataSourceBo1_1.setAgentId("test_agent1");
        dataSourceBo1_1.setTimestamp(collectTime1st);
        dataSourceBo1_1.setServiceTypeCode((short) 1000);
        dataSourceBo1_1.setJdbcUrl("jdbc:mysql");
        dataSourceBo1_1.setActiveConnectionSize(15);
        dataSourceBo1_1.setMaxConnectionSize(30);
        dataSourceBo1_1.setId(1);
        dataSourceBo1_1.setDatabaseName("pinpoint1");
        DataSourceBo dataSourceBo1_2 = new DataSourceBo();
        dataSourceBo1_2.setAgentId("test_agent1");
        dataSourceBo1_2.setTimestamp(collectTime1st);
        dataSourceBo1_2.setAgentId("test_agent1");
        dataSourceBo1_2.setServiceTypeCode((short) 2000);
        dataSourceBo1_2.setJdbcUrl("jdbc:mssql");
        dataSourceBo1_2.setActiveConnectionSize(25);
        dataSourceBo1_2.setMaxConnectionSize(40);
        dataSourceBo1_2.setId(2);
        dataSourceBo1_2.setDatabaseName("pinpoint2");
        dataSourceListBo1.add(dataSourceBo1_1);
        dataSourceListBo1.add(dataSourceBo1_2);

        DataSourceListBo dataSourceListBo2 = new DataSourceListBo();
        dataSourceListBo2.setAgentId("test_agent1");
        dataSourceListBo2.setStartTimestamp(startTimestamp);
        dataSourceListBo2.setTimestamp(collectTime2nd);

        DataSourceBo dataSourceBo2_1 = new DataSourceBo();
        dataSourceBo2_1.setAgentId("test_agent1");
        dataSourceBo2_1.setTimestamp(collectTime2nd);
        dataSourceBo2_1.setServiceTypeCode((short) 1000);
        dataSourceBo2_1.setJdbcUrl("jdbc:mysql");
        dataSourceBo2_1.setActiveConnectionSize(16);
        dataSourceBo2_1.setMaxConnectionSize(31);
        dataSourceBo2_1.setId(1);
        dataSourceBo2_1.setDatabaseName("pinpoint1");
        DataSourceBo dataSourceBo2_2 = new DataSourceBo();
        dataSourceBo2_2.setAgentId("test_agent1");
        dataSourceBo2_2.setTimestamp(collectTime2nd);
        dataSourceBo2_2.setAgentId("test_agent1");
        dataSourceBo2_2.setServiceTypeCode((short) 2000);
        dataSourceBo2_2.setJdbcUrl("jdbc:mssql");
        dataSourceBo2_2.setActiveConnectionSize(26);
        dataSourceBo2_2.setMaxConnectionSize(41);
        dataSourceBo2_2.setId(2);
        dataSourceBo2_2.setDatabaseName("pinpoint2");
        dataSourceListBo2.add(dataSourceBo2_1);
        dataSourceListBo2.add(dataSourceBo2_2);

        dataSourceListBoList.add(dataSourceListBo1);
        dataSourceListBoList.add(dataSourceListBo2);

        return dataSourceListBoList;
    }

    @Test
    public void map3Test() throws Exception {
        final AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setStartTimestamp(startTimestamp);
        agentStatBo.setAgentId(TEST_AGENT);
        agentStatBo.setJvmGcBos(createJvmGcBoList());

        List<TFAgentStat> tFAgentStatList = new TFAgentStatMapper().map(agentStatBo);
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

    private List<JvmGcBo> createJvmGcBoList() {
        List<JvmGcBo> jvmGcBoList = new ArrayList<>();

        JvmGcBo jvmGcBo1 = new JvmGcBo();
        jvmGcBo1.setAgentId("test_agent1");
        jvmGcBo1.setStartTimestamp(startTimestamp);
        jvmGcBo1.setTimestamp(collectTime1st);
        jvmGcBo1.setGcType(JvmGcType.G1);
        jvmGcBo1.setHeapUsed(3000);
        jvmGcBo1.setHeapMax(5000);
        jvmGcBo1.setNonHeapUsed(300);
        jvmGcBo1.setNonHeapMax(500);
        jvmGcBo1.setGcOldCount(5);
        jvmGcBo1.setGcOldTime(10);
        jvmGcBoList.add(jvmGcBo1);

        JvmGcBo jvmGcBo2 = new JvmGcBo();
        jvmGcBo2.setAgentId("test_agent1");
        jvmGcBo2.setStartTimestamp(startTimestamp);
        jvmGcBo2.setTimestamp(collectTime2nd);
        jvmGcBo2.setGcType(JvmGcType.G1);
        jvmGcBo2.setHeapUsed(3100);
        jvmGcBo2.setHeapMax(5100);
        jvmGcBo2.setNonHeapUsed(310);
        jvmGcBo2.setNonHeapMax(510);
        jvmGcBo2.setGcOldCount(15);
        jvmGcBo2.setGcOldTime(20);
        jvmGcBoList.add(jvmGcBo2);

        return jvmGcBoList;
    }

    @Test
    public void map4Test() {
        final AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setStartTimestamp(startTimestamp);
        agentStatBo.setAgentId(TEST_AGENT);
        agentStatBo.setActiveTraceBos(createActiveTraceBoList());

        List<TFAgentStat> tFAgentStatList = new TFAgentStatMapper().map(agentStatBo);
        assertEquals(2, tFAgentStatList.size());

        TFAgentStat tFAgentStat1 = tFAgentStatList.get(0);
        assertEquals(tFAgentStat1.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat1.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat1.getTimestamp(), collectTime1st);
        TFActiveTrace activeTrace1 = tFAgentStat1.getActiveTrace();
        TFActiveTraceHistogram histogram1 = activeTrace1.getHistogram();
        List<Integer> activeTraceCount1 = histogram1.getActiveTraceCount();
        assertEquals((int)activeTraceCount1.get(0), 30);
        assertEquals((int)activeTraceCount1.get(1), 40);
        assertEquals((int)activeTraceCount1.get(2), 10);
        assertEquals((int)activeTraceCount1.get(3), 50);

        TFAgentStat tFAgentStat2 = tFAgentStatList.get(1);
        assertEquals(tFAgentStat2.getAgentId(), TEST_AGENT);
        assertEquals(tFAgentStat2.getStartTimestamp(), startTimestamp);
        assertEquals(tFAgentStat2.getTimestamp(), collectTime2nd);
        TFActiveTrace activeTrace2 = tFAgentStat2.getActiveTrace();
        TFActiveTraceHistogram histogram2 = activeTrace2.getHistogram();
        List<Integer> activeTraceCount2 = histogram2.getActiveTraceCount();
        assertEquals((int)activeTraceCount2.get(0), 31);
        assertEquals((int)activeTraceCount2.get(1), 41);
        assertEquals((int)activeTraceCount2.get(2), 11);
        assertEquals((int)activeTraceCount2.get(3), 51);
    }

    private List<ActiveTraceBo> createActiveTraceBoList() {
        List<ActiveTraceBo> activeTraceBoList = new ArrayList<>();

        ActiveTraceBo activeTraceBo1 = new ActiveTraceBo();
        activeTraceBo1.setAgentId("test_agent1");
        activeTraceBo1.setStartTimestamp(startTimestamp);
        activeTraceBo1.setTimestamp(collectTime1st);
        activeTraceBo1.setVersion((short) 1);
        activeTraceBo1.setHistogramSchemaType(2);
        ActiveTraceHistogram activeTraceHistogram1 = new ActiveTraceHistogram(30, 40, 10, 50);
        activeTraceBo1.setActiveTraceHistogram(activeTraceHistogram1);
        activeTraceBoList.add(activeTraceBo1);

        ActiveTraceBo activeTraceBo2 = new ActiveTraceBo();
        activeTraceBo2.setAgentId("test_agent1");
        activeTraceBo2.setStartTimestamp(startTimestamp);
        activeTraceBo2.setTimestamp(collectTime2nd);
        activeTraceBo2.setVersion((short) 1);
        activeTraceBo2.setHistogramSchemaType(2);

        ActiveTraceHistogram activeTraceHistogram2 = new ActiveTraceHistogram(31, 41, 11, 51);
        activeTraceBo2.setActiveTraceHistogram(activeTraceHistogram2);
        activeTraceBoList.add(activeTraceBo2);

        return activeTraceBoList;
    }

    @Test
    public void map5Test() {
        final AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setStartTimestamp(startTimestamp);
        agentStatBo.setAgentId(TEST_AGENT);
        agentStatBo.setResponseTimeBos(createResponseTimeBoList());

        List<TFAgentStat> tFAgentStatList = new TFAgentStatMapper().map(agentStatBo);
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

    private List<ResponseTimeBo> createResponseTimeBoList() {
        List<ResponseTimeBo> responseTimeBoList = new ArrayList<>();

        ResponseTimeBo responseTimeBo1 = new ResponseTimeBo();
        responseTimeBo1.setAvg(1000);
        responseTimeBo1.setStartTimestamp(startTimestamp);
        responseTimeBo1.setAgentId(TEST_AGENT);
        responseTimeBo1.setTimestamp(collectTime1st);
        responseTimeBoList.add(responseTimeBo1);

        ResponseTimeBo responseTimeBo2 = new ResponseTimeBo();
        responseTimeBo2.setAvg(2000);
        responseTimeBo2.setStartTimestamp(startTimestamp);
        responseTimeBo2.setAgentId(TEST_AGENT);
        responseTimeBo2.setTimestamp(collectTime2nd);
        responseTimeBoList.add(responseTimeBo2);

        return responseTimeBoList;
    }

    @Test
    public void map6Test() throws Exception {
        final AgentStatBo agentStatBo = new AgentStatBo();
        agentStatBo.setStartTimestamp(startTimestamp);
        agentStatBo.setAgentId(TEST_AGENT);
        agentStatBo.setFileDescriptorBos(createFileDescriptorBoList());

        List<TFAgentStat> tFAgentStatList = new TFAgentStatMapper().map(agentStatBo);
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

    private List<FileDescriptorBo> createFileDescriptorBoList() {
        final List<FileDescriptorBo> fileDescriptorBoList = new ArrayList<>();

        FileDescriptorBo fileDescriptorBo1 = new FileDescriptorBo();
        fileDescriptorBo1.setAgentId(TEST_AGENT);
        fileDescriptorBo1.setTimestamp(collectTime1st);
        fileDescriptorBo1.setStartTimestamp(startTimestamp);
        fileDescriptorBo1.setOpenFileDescriptorCount(4);
        fileDescriptorBoList.add(fileDescriptorBo1);

        FileDescriptorBo fileDescriptorBo2 = new FileDescriptorBo();
        fileDescriptorBo2.setAgentId(TEST_AGENT);
        fileDescriptorBo2.setTimestamp(collectTime2nd);
        fileDescriptorBo2.setStartTimestamp(startTimestamp);
        fileDescriptorBo2.setOpenFileDescriptorCount(5);
        fileDescriptorBoList.add(fileDescriptorBo2);

        FileDescriptorBo fileDescriptorBo3 = new FileDescriptorBo();
        fileDescriptorBo3.setAgentId(TEST_AGENT);
        fileDescriptorBo3.setTimestamp(collectTime3rd);
        fileDescriptorBo3.setStartTimestamp(startTimestamp);
        fileDescriptorBo3.setOpenFileDescriptorCount(8);
        fileDescriptorBoList.add(fileDescriptorBo3);

        return fileDescriptorBoList;
    }

}