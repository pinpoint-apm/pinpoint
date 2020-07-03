/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.flink.process;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.flink.mapper.thrift.stat.JoinAgentStatBoMapper;
import com.navercorp.pinpoint.flink.vo.RawData;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.flink.TFCpuLoad;
import com.navercorp.pinpoint.thrift.dto.flink.TFDirectBuffer;
import com.navercorp.pinpoint.thrift.dto.flink.TFFileDescriptor;
import com.navercorp.pinpoint.thrift.dto.flink.TFJvmGc;
import com.navercorp.pinpoint.thrift.dto.flink.TFTransaction;

import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author minwoo.jung
 */
public class TBaseFlatMapperTest {
    final static String AGENT_ID = "testAgent";
    final static String APPLICATION_ID = "testApplication";

    @Test
    public void flatMapTest() throws Exception {

        ApplicationCache applicationCache = newMockApplicationCache();
        TBaseFlatMapper mapper = new TBaseFlatMapper(new JoinAgentStatBoMapper(), applicationCache, new DefaultTBaseFlatMapperInterceptor());


        TFAgentStatBatch tfAgentStatBatch = createTFAgentStatBatch();
        ArrayList<Tuple3<String, JoinStatBo, Long>> dataList = new ArrayList<>();
        ListCollector<Tuple3<String, JoinStatBo, Long>> collector = new ListCollector<>(dataList);
        RawData rawData = newRawData(tfAgentStatBatch);
        mapper.flatMap(rawData, collector);

        assertEquals(dataList.size(), 2);

        Tuple3<String, JoinStatBo, Long> data1 = dataList.get(0);
        assertEquals(data1.f0, AGENT_ID);
        assertEquals(data1.f2.longValue(), 1491274143454L);
        JoinAgentStatBo joinAgentStatBo = (JoinAgentStatBo) data1.f1;
        assertEquals(joinAgentStatBo.getId(), AGENT_ID);
        assertEquals(joinAgentStatBo.getAgentStartTimestamp(), 1491274142454L);
        assertEquals(joinAgentStatBo.getTimestamp(), 1491274143454L);
        assertJoinCpuLoadBo(joinAgentStatBo.getJoinCpuLoadBoList());

        Tuple3<String, JoinStatBo, Long> data2 = dataList.get(1);
        assertEquals(data2.f0, APPLICATION_ID);
        assertEquals(data2.f2.longValue(), 1491274140000L);
        JoinApplicationStatBo joinApplicationStatBo = (JoinApplicationStatBo) data2.f1;
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), 1491274140000L);
        assertEquals(joinApplicationStatBo.getStatType(), StatType.APP_STST);
        assertJoinCpuLoadBo(joinApplicationStatBo.getJoinCpuLoadBoList());
    }

    private ApplicationCache newMockApplicationCache() {
        ApplicationCache applicationCache = mock(ApplicationCache.class);
        when(applicationCache.findApplicationId(any(ApplicationCache.ApplicationKey.class)))
            .thenReturn(APPLICATION_ID);
        return applicationCache;
    }

    private void assertJoinCpuLoadBo(List<JoinCpuLoadBo> joincpulaodBoList) {
        assertEquals(2, joincpulaodBoList.size());
        JoinCpuLoadBo joinCpuLoadBo = joincpulaodBoList.get(0);
        assertEquals(joinCpuLoadBo.getId(), AGENT_ID);
        assertEquals(joinCpuLoadBo.getTimestamp(), 1491274143454L);
        JoinDoubleFieldBo jvmCpuLoadJoinValue = joinCpuLoadBo.getJvmCpuLoadJoinValue();
        assertEquals(jvmCpuLoadJoinValue.getAvg(), 10, 0);
        assertEquals(jvmCpuLoadJoinValue.getMin(), 10, 0);
        assertEquals(jvmCpuLoadJoinValue.getMax(), 10, 0);
        JoinDoubleFieldBo systemCpuLoadJoinValue = joinCpuLoadBo.getSystemCpuLoadJoinValue();
        assertEquals(systemCpuLoadJoinValue.getAvg(), 30, 0);
        assertEquals(systemCpuLoadJoinValue.getMin(), 30, 0);
        assertEquals(systemCpuLoadJoinValue.getMax(), 30, 0);
        joinCpuLoadBo = joincpulaodBoList.get(1);
        assertEquals(joinCpuLoadBo.getId(), AGENT_ID);
        assertEquals(joinCpuLoadBo.getTimestamp(), 1491274148454L);
        jvmCpuLoadJoinValue = joinCpuLoadBo.getJvmCpuLoadJoinValue();
        assertEquals(jvmCpuLoadJoinValue.getAvg(), 20, 0);
        assertEquals(jvmCpuLoadJoinValue.getMin(), 20, 0);
        assertEquals(jvmCpuLoadJoinValue.getMax(), 20, 0);
        systemCpuLoadJoinValue = joinCpuLoadBo.getSystemCpuLoadJoinValue();
        assertEquals(systemCpuLoadJoinValue.getAvg(), 50, 0);
        assertEquals(systemCpuLoadJoinValue.getMin(), 50, 0);
        assertEquals(systemCpuLoadJoinValue.getMax(), 50, 0);
    }

    private TFAgentStatBatch createTFAgentStatBatch() {
        final TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch();
        tFAgentStatBatch.setStartTimestamp(1491274142454L);
        tFAgentStatBatch.setAgentId(AGENT_ID);

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(AGENT_ID);
        tFAgentStat.setTimestamp(1491274143454L);

        final TFCpuLoad tFCpuLoad = new TFCpuLoad();
        tFCpuLoad.setJvmCpuLoad(10);
        tFCpuLoad.setSystemCpuLoad(30);
        tFAgentStat.setCpuLoad(tFCpuLoad);

        final TFAgentStat tFAgentStat2 = new TFAgentStat();
        tFAgentStat2.setAgentId(AGENT_ID);
        tFAgentStat2.setTimestamp(1491274148454L);

        final TFCpuLoad tFCpuLoad2 = new TFCpuLoad();
        tFCpuLoad2.setJvmCpuLoad(20);
        tFCpuLoad2.setSystemCpuLoad(50);
        tFAgentStat2.setCpuLoad(tFCpuLoad2);

        final List<TFAgentStat> tFAgentStatList = new ArrayList<>(2);
        tFAgentStatList.add(tFAgentStat);
        tFAgentStatList.add(tFAgentStat2);
        tFAgentStatBatch.setAgentStats(tFAgentStatList);

        return tFAgentStatBatch;
    }



    @Test
    public void flatMap2Test() throws Exception {
        ApplicationCache applicationCache = newMockApplicationCache();
        TBaseFlatMapper mapper = new TBaseFlatMapper(new JoinAgentStatBoMapper(), applicationCache, new DefaultTBaseFlatMapperInterceptor());

        TFAgentStatBatch tfAgentStatBatch = createTFAgentStatBatch2();
        ArrayList<Tuple3<String, JoinStatBo, Long>> dataList = new ArrayList<>();
        ListCollector<Tuple3<String, JoinStatBo, Long>> collector = new ListCollector<>(dataList);
        RawData rawdata = newRawData(tfAgentStatBatch);
        mapper.flatMap(rawdata, collector);

        assertEquals(dataList.size(), 2);

        Tuple3<String, JoinStatBo, Long> data1 = dataList.get(0);
        assertEquals(data1.f0, AGENT_ID);
        assertEquals(data1.f2.longValue(), 1491274143454L);
        JoinAgentStatBo joinAgentStatBo = (JoinAgentStatBo) data1.f1;
        assertEquals(joinAgentStatBo.getId(), AGENT_ID);
        assertEquals(joinAgentStatBo.getAgentStartTimestamp(), 1491274142454L);
        assertEquals(joinAgentStatBo.getTimestamp(), 1491274143454L);
        assertJoinMemoryBo(joinAgentStatBo.getJoinMemoryBoList());

        Tuple3<String, JoinStatBo, Long> data2 = dataList.get(1);
        assertEquals(data2.f0, APPLICATION_ID);
        assertEquals(data2.f2.longValue(), 1491274140000L);
        JoinApplicationStatBo joinApplicationStatBo = (JoinApplicationStatBo) data2.f1;
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), 1491274140000L);
        assertEquals(joinApplicationStatBo.getStatType(), StatType.APP_STST);
        assertJoinMemoryBo(joinApplicationStatBo.getJoinMemoryBoList());
    }

    private void assertJoinMemoryBo(List<JoinMemoryBo> joinMemoryBoList) {
        assertEquals(2, joinMemoryBoList.size());

        JoinMemoryBo joinMemoryBo = joinMemoryBoList.get(0);
        assertEquals(joinMemoryBo.getId(), AGENT_ID);
        assertEquals((long) joinMemoryBo.getHeapUsedJoinValue().getAvg(), 3000);
        assertEquals((long) joinMemoryBo.getNonHeapUsedJoinValue().getAvg(), 450);
        assertEquals(joinMemoryBo.getTimestamp(), 1491274143454L);

        JoinMemoryBo joinMemoryBo2 = joinMemoryBoList.get(1);
        assertEquals(joinMemoryBo2.getId(), AGENT_ID);
        assertEquals((long) joinMemoryBo2.getHeapUsedJoinValue().getAvg(), 2000);
        assertEquals((long) joinMemoryBo2.getNonHeapUsedJoinValue().getAvg(), 850);
        assertEquals(joinMemoryBo2.getTimestamp(), 1491274148454L);
    }

    private TFAgentStatBatch createTFAgentStatBatch2() {
        final TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch();
        tFAgentStatBatch.setStartTimestamp(1491274142454L);
        tFAgentStatBatch.setAgentId(AGENT_ID);

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(AGENT_ID);
        tFAgentStat.setTimestamp(1491274143454L);

        final TFJvmGc tFJvmGc = new TFJvmGc();
        tFJvmGc.setJvmMemoryHeapUsed(3000);
        tFJvmGc.setJvmMemoryNonHeapUsed(450);
        tFAgentStat.setGc(tFJvmGc);

        final TFAgentStat tFAgentStat2 = new TFAgentStat();
        tFAgentStat2.setAgentId(AGENT_ID);
        tFAgentStat2.setTimestamp(1491274148454L);

        final TFJvmGc tFJvmGc2 = new TFJvmGc();
        tFJvmGc2.setJvmMemoryHeapUsed(2000);
        tFJvmGc2.setJvmMemoryNonHeapUsed(850);
        tFAgentStat2.setGc(tFJvmGc2);

        final List<TFAgentStat> tFAgentStatList = new ArrayList<>(2);
        tFAgentStatList.add(tFAgentStat);
        tFAgentStatList.add(tFAgentStat2);
        tFAgentStatBatch.setAgentStats(tFAgentStatList);

        return tFAgentStatBatch;
    }

    @Test
    public void flatMap3Test() throws Exception {
        ApplicationCache applicationCache = newMockApplicationCache();
        TBaseFlatMapper mapper = new TBaseFlatMapper(new JoinAgentStatBoMapper(), applicationCache, new DefaultTBaseFlatMapperInterceptor());

        TFAgentStatBatch tfAgentStatBatch = createTFAgentStatBatch3();
        ArrayList<Tuple3<String, JoinStatBo, Long>> dataList = new ArrayList<>();
        ListCollector<Tuple3<String, JoinStatBo, Long>> collector = new ListCollector<>(dataList);
        RawData rawData = newRawData(tfAgentStatBatch);
        mapper.flatMap(rawData, collector);

        assertEquals(dataList.size(), 2);

        Tuple3<String, JoinStatBo, Long> data1 = dataList.get(0);
        assertEquals(data1.f0, AGENT_ID);
        assertEquals(data1.f2.longValue(), 1491274143454L);
        JoinAgentStatBo joinAgentStatBo = (JoinAgentStatBo) data1.f1;
        assertEquals(joinAgentStatBo.getId(), AGENT_ID);
        assertEquals(joinAgentStatBo.getAgentStartTimestamp(), 1491274142454L);
        assertEquals(joinAgentStatBo.getTimestamp(), 1491274143454L);
        assertJoinTransactionBo(joinAgentStatBo.getJoinTransactionBoList());

        Tuple3<String, JoinStatBo, Long> data2 = dataList.get(1);
        assertEquals(data2.f0, APPLICATION_ID);
        assertEquals(data2.f2.longValue(), 1491274140000L);
        JoinApplicationStatBo joinApplicationStatBo = (JoinApplicationStatBo) data2.f1;
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), 1491274140000L);
        assertEquals(joinApplicationStatBo.getStatType(), StatType.APP_STST);
        assertJoinTransactionBo(joinApplicationStatBo.getJoinTransactionBoList());
    }

    private RawData newRawData(TFAgentStatBatch tfAgentStatBatch) {
        return new RawData(tfAgentStatBatch, Collections.emptyMap());
    }

    private void assertJoinTransactionBo(List<JoinTransactionBo> joinTransactionBoList) {
        assertEquals(2, joinTransactionBoList.size());

        JoinTransactionBo joinTransactionBo = joinTransactionBoList.get(0);
        assertEquals(joinTransactionBo.getId(), AGENT_ID);
        assertEquals(joinTransactionBo.getTimestamp(), 1491274143454L);
        assertEquals(joinTransactionBo.getCollectInterval(), 5000);
        assertEquals(joinTransactionBo.getTotalCountJoinValue(), new JoinLongFieldBo(120L, 120L, AGENT_ID, 120L, AGENT_ID));

        JoinTransactionBo joinTransactionBo2 = joinTransactionBoList.get(1);
        assertEquals(joinTransactionBo2.getId(), AGENT_ID);
        assertEquals(joinTransactionBo2.getTimestamp(), 1491274148454L);
        assertEquals(joinTransactionBo2.getCollectInterval(), 5000);
        assertEquals(joinTransactionBo2.getTotalCountJoinValue(), new JoinLongFieldBo(124L, 124L, AGENT_ID, 124L, AGENT_ID));
    }

    private TFAgentStatBatch createTFAgentStatBatch3() {
        final TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch();
        tFAgentStatBatch.setStartTimestamp(1491274142454L);
        tFAgentStatBatch.setAgentId(AGENT_ID);

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(AGENT_ID);
        tFAgentStat.setTimestamp(1491274143454L);
        tFAgentStat.setCollectInterval(5000);

        final TFTransaction tFTransaction = new TFTransaction();
        tFTransaction.setSampledNewCount(10);
        tFTransaction.setSampledContinuationCount(20);
        tFTransaction.setUnsampledNewCount(40);
        tFTransaction.setUnsampledContinuationCount(50);
        tFAgentStat.setTransaction(tFTransaction);

        final TFAgentStat tFAgentStat2 = new TFAgentStat();
        tFAgentStat2.setAgentId(AGENT_ID);
        tFAgentStat2.setTimestamp(1491274148454L);
        tFAgentStat2.setCollectInterval(5000);

        final TFTransaction tFTransaction2 = new TFTransaction();
        tFTransaction2.setSampledNewCount(11);
        tFTransaction2.setSampledContinuationCount(21);
        tFTransaction2.setUnsampledNewCount(41);
        tFTransaction2.setUnsampledContinuationCount(51);
        tFAgentStat2.setTransaction(tFTransaction2);

        final List<TFAgentStat> tFAgentStatList = new ArrayList<>(2);
        tFAgentStatList.add(tFAgentStat);
        tFAgentStatList.add(tFAgentStat2);
        tFAgentStatBatch.setAgentStats(tFAgentStatList);

        return tFAgentStatBatch;
    }

    @Test
    public void flatMap4Test() throws Exception {

        ApplicationCache applicationCache = newMockApplicationCache();
        TBaseFlatMapper mapper = new TBaseFlatMapper(new JoinAgentStatBoMapper(), applicationCache, new DefaultTBaseFlatMapperInterceptor());


        TFAgentStatBatch tfAgentStatBatch = createTFAgentStatBatch4();
        ArrayList<Tuple3<String, JoinStatBo, Long>> dataList = new ArrayList<>();
        ListCollector<Tuple3<String, JoinStatBo, Long>> collector = new ListCollector<>(dataList);
        RawData rawData = newRawData(tfAgentStatBatch);
        mapper.flatMap(rawData, collector);

        assertEquals(dataList.size(), 2);

        Tuple3<String, JoinStatBo, Long> data1 = dataList.get(0);
        assertEquals(data1.f0, AGENT_ID);
        assertEquals(data1.f2.longValue(), 1491274143454L);
        JoinAgentStatBo joinAgentStatBo = (JoinAgentStatBo) data1.f1;
        assertEquals(joinAgentStatBo.getId(), AGENT_ID);
        assertEquals(joinAgentStatBo.getAgentStartTimestamp(), 1491274142454L);
        assertEquals(joinAgentStatBo.getTimestamp(), 1491274143454L);
        assertJoinFileDescriptorBo(joinAgentStatBo.getJoinFileDescriptorBoList());

        Tuple3<String, JoinStatBo, Long> data2 = dataList.get(1);
        assertEquals(data2.f0, APPLICATION_ID);
        assertEquals(data2.f2.longValue(), 1491274140000L);
        JoinApplicationStatBo joinApplicationStatBo = (JoinApplicationStatBo) data2.f1;
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), 1491274140000L);
        assertEquals(joinApplicationStatBo.getStatType(), StatType.APP_STST);
        assertJoinFileDescriptorBo(joinApplicationStatBo.getJoinFileDescriptorBoList());
    }

    private void assertJoinFileDescriptorBo(List<JoinFileDescriptorBo> joinFileDescriptorBoList) {
        assertEquals(2, joinFileDescriptorBoList.size());
        JoinFileDescriptorBo joinFileDescriptorBo = joinFileDescriptorBoList.get(0);
        assertEquals(joinFileDescriptorBo.getId(), AGENT_ID);
        assertEquals(joinFileDescriptorBo.getTimestamp(), 1491274143454L);
        assertEquals(joinFileDescriptorBo.getOpenFdCountJoinValue().getAvg(), 10, 0);
        assertEquals(joinFileDescriptorBo.getOpenFdCountJoinValue().getMin(), 10, 0);
        assertEquals(joinFileDescriptorBo.getOpenFdCountJoinValue().getMax(), 10, 0);
        joinFileDescriptorBo = joinFileDescriptorBoList.get(1);
        assertEquals(joinFileDescriptorBo.getId(), AGENT_ID);
        assertEquals(joinFileDescriptorBo.getTimestamp(), 1491274148454L);
        assertEquals(joinFileDescriptorBo.getOpenFdCountJoinValue().getAvg(), 20, 0);
        assertEquals(joinFileDescriptorBo.getOpenFdCountJoinValue().getMin(), 20, 0);
        assertEquals(joinFileDescriptorBo.getOpenFdCountJoinValue().getMax(), 20, 0);
    }

    private TFAgentStatBatch createTFAgentStatBatch4() {
        final TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch();
        tFAgentStatBatch.setStartTimestamp(1491274142454L);
        tFAgentStatBatch.setAgentId(AGENT_ID);

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(AGENT_ID);
        tFAgentStat.setTimestamp(1491274143454L);

        final TFFileDescriptor tFFileDescriptor = new TFFileDescriptor();
        tFFileDescriptor.setOpenFileDescriptorCount(10);
        tFAgentStat.setFileDescriptor(tFFileDescriptor);

        final TFAgentStat tFAgentStat2 = new TFAgentStat();
        tFAgentStat2.setAgentId(AGENT_ID);
        tFAgentStat2.setTimestamp(1491274148454L);

        final TFFileDescriptor tFFileDescriptor2 = new TFFileDescriptor();
        tFFileDescriptor2.setOpenFileDescriptorCount(20);
        tFAgentStat2.setFileDescriptor(tFFileDescriptor2);

        final List<TFAgentStat> tFAgentStatList = new ArrayList<>(2);
        tFAgentStatList.add(tFAgentStat);
        tFAgentStatList.add(tFAgentStat2);
        tFAgentStatBatch.setAgentStats(tFAgentStatList);

        return tFAgentStatBatch;
    }


    @Test
    public void flatMap5Test() throws Exception {

        ApplicationCache applicationCache = newMockApplicationCache();
        TBaseFlatMapper mapper = new TBaseFlatMapper(new JoinAgentStatBoMapper(), applicationCache, new DefaultTBaseFlatMapperInterceptor());


        TFAgentStatBatch tfAgentStatBatch = createTFAgentStatBatch5();
        ArrayList<Tuple3<String, JoinStatBo, Long>> dataList = new ArrayList<>();
        ListCollector<Tuple3<String, JoinStatBo, Long>> collector = new ListCollector<>(dataList);
        RawData rawData = newRawData(tfAgentStatBatch);
        mapper.flatMap(rawData, collector);

        assertEquals(dataList.size(), 2);

        Tuple3<String, JoinStatBo, Long> data1 = dataList.get(0);
        assertEquals(data1.f0, AGENT_ID);
        assertEquals(data1.f2.longValue(), 1491274143454L);
        JoinAgentStatBo joinAgentStatBo = (JoinAgentStatBo) data1.f1;
        assertEquals(joinAgentStatBo.getId(), AGENT_ID);
        assertEquals(joinAgentStatBo.getAgentStartTimestamp(), 1491274142454L);
        assertEquals(joinAgentStatBo.getTimestamp(), 1491274143454L);
        assertJoinDirectBufferBo(joinAgentStatBo.getJoinDirectBufferBoList());

        Tuple3<String, JoinStatBo, Long> data2 = dataList.get(1);
        assertEquals(data2.f0, APPLICATION_ID);
        assertEquals(data2.f2.longValue(), 1491274140000L);
        JoinApplicationStatBo joinApplicationStatBo = (JoinApplicationStatBo) data2.f1;
        assertEquals(joinApplicationStatBo.getId(), APPLICATION_ID);
        assertEquals(joinApplicationStatBo.getTimestamp(), 1491274140000L);
        assertEquals(joinApplicationStatBo.getStatType(), StatType.APP_STST);
        assertJoinDirectBufferBo(joinApplicationStatBo.getJoinDirectBufferBoList());
    }

    private void assertJoinDirectBufferBo(List<JoinDirectBufferBo> joinDirectBufferBoList) {
        assertEquals(2, joinDirectBufferBoList.size());
        JoinDirectBufferBo joinDirectBufferBo = joinDirectBufferBoList.get(0);
        assertEquals(joinDirectBufferBo.getId(), AGENT_ID);
        assertEquals(joinDirectBufferBo.getTimestamp(), 1491274143454L);
        JoinLongFieldBo directCountJoinValue = joinDirectBufferBo.getDirectCountJoinValue();
        assertEquals(directCountJoinValue.getAvg(), 10, 0);
        assertEquals(directCountJoinValue.getMin(), 10, 0);
        assertEquals(directCountJoinValue.getMax(), 10, 0);
        JoinLongFieldBo directMemoryUsedJoinValue = joinDirectBufferBo.getDirectMemoryUsedJoinValue();
        assertEquals(directMemoryUsedJoinValue.getAvg(), 20, 0);
        assertEquals(directMemoryUsedJoinValue.getMin(), 20, 0);
        assertEquals(directMemoryUsedJoinValue.getMax(), 20, 0);
        JoinLongFieldBo mappedCountJoinValue = joinDirectBufferBo.getMappedCountJoinValue();
        assertEquals(mappedCountJoinValue.getAvg(), 30, 0);
        assertEquals(mappedCountJoinValue.getMin(), 30, 0);
        assertEquals(mappedCountJoinValue.getMax(), 30, 0);
        JoinLongFieldBo mappedMemoryUsedJoinValue = joinDirectBufferBo.getMappedMemoryUsedJoinValue();
        assertEquals(mappedMemoryUsedJoinValue.getAvg(), 40, 0);
        assertEquals(mappedMemoryUsedJoinValue.getMin(), 40, 0);
        assertEquals(mappedMemoryUsedJoinValue.getMax(), 40, 0);
        joinDirectBufferBo = joinDirectBufferBoList.get(1);
        assertEquals(joinDirectBufferBo.getId(), AGENT_ID);
        assertEquals(joinDirectBufferBo.getTimestamp(), 1491274148454L);
        directCountJoinValue = joinDirectBufferBo.getDirectCountJoinValue();
        assertEquals(directCountJoinValue.getAvg(), 50, 0);
        assertEquals(directCountJoinValue.getMin(), 50, 0);
        assertEquals(directCountJoinValue.getMax(), 50, 0);
        directMemoryUsedJoinValue = joinDirectBufferBo.getDirectMemoryUsedJoinValue();
        assertEquals(directMemoryUsedJoinValue.getAvg(), 60, 0);
        assertEquals(directMemoryUsedJoinValue.getMin(), 60, 0);
        assertEquals(directMemoryUsedJoinValue.getMax(), 60, 0);
        mappedCountJoinValue = joinDirectBufferBo.getMappedCountJoinValue();
        assertEquals(mappedCountJoinValue.getAvg(), 70, 0);
        assertEquals(mappedCountJoinValue.getMin(), 70, 0);
        assertEquals(mappedCountJoinValue.getMax(), 70, 0);
        mappedMemoryUsedJoinValue = joinDirectBufferBo.getMappedMemoryUsedJoinValue();
        assertEquals(mappedMemoryUsedJoinValue.getAvg(), 80, 0);
        assertEquals(mappedMemoryUsedJoinValue.getMin(), 80, 0);
        assertEquals(mappedMemoryUsedJoinValue.getMax(), 80, 0);
    }

    private TFAgentStatBatch createTFAgentStatBatch5() {
        final TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch();
        tFAgentStatBatch.setStartTimestamp(1491274142454L);
        tFAgentStatBatch.setAgentId(AGENT_ID);

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(AGENT_ID);
        tFAgentStat.setTimestamp(1491274143454L);

        final TFDirectBuffer tFDirectBuffer = new TFDirectBuffer();
        tFDirectBuffer.setDirectCount(10);
        tFDirectBuffer.setDirectMemoryUsed(20);
        tFDirectBuffer.setMappedCount(30);
        tFDirectBuffer.setMappedMemoryUsed(40);
        tFAgentStat.setDirectBuffer(tFDirectBuffer);

        final TFAgentStat tFAgentStat2 = new TFAgentStat();
        tFAgentStat2.setAgentId(AGENT_ID);
        tFAgentStat2.setTimestamp(1491274148454L);

        final TFDirectBuffer tFDirectBuffer2 = new TFDirectBuffer();
        tFDirectBuffer2.setDirectCount(50);
        tFDirectBuffer2.setDirectMemoryUsed(60);
        tFDirectBuffer2.setMappedCount(70);
        tFDirectBuffer2.setMappedMemoryUsed(80);
        tFAgentStat2.setDirectBuffer(tFDirectBuffer2);

        final List<TFAgentStat> tFAgentStatList = new ArrayList<>(2);
        tFAgentStatList.add(tFAgentStat);
        tFAgentStatList.add(tFAgentStat2);
        tFAgentStatBatch.setAgentStats(tFAgentStatList);

        return tFAgentStatBatch;
    }

    @Test
    public void test() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        System.out.println(data);
    }
}