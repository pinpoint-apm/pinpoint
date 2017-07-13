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

package com.navercorp.pinpoint.flink.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.thrift.dto.flink.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBoMapperTest {

    @Test
    public void mapTest() {
        final String agentId = "testAgent";
        final JoinAgentStatBoMapper joinAgentStatBoMapper = new JoinAgentStatBoMapper();

        final TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch();
        tFAgentStatBatch.setStartTimestamp(1491274138454L);
        tFAgentStatBatch.setAgentId(agentId);

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(agentId);
        tFAgentStat.setTimestamp(1491274148454L);

        final TFCpuLoad tFCpuLoad = new TFCpuLoad();
        tFCpuLoad.setJvmCpuLoad(10);
        tFCpuLoad.setSystemCpuLoad(30);
        tFAgentStat.setCpuLoad(tFCpuLoad);

        final TFAgentStat tFAgentStat2 = new TFAgentStat();
        tFAgentStat2.setAgentId(agentId);
        tFAgentStat2.setTimestamp(1491275148454L);

        final TFCpuLoad tFCpuLoad2 = new TFCpuLoad();
        tFCpuLoad2.setJvmCpuLoad(20);
        tFCpuLoad2.setSystemCpuLoad(50);
        tFAgentStat2.setCpuLoad(tFCpuLoad2);

        final List<TFAgentStat> tFAgentStatList = new ArrayList<>(2);
        tFAgentStatList.add(tFAgentStat);
        tFAgentStatList.add(tFAgentStat2);
        tFAgentStatBatch.setAgentStats(tFAgentStatList);

        JoinAgentStatBo joinAgentStatBo = joinAgentStatBoMapper.map(tFAgentStatBatch);
        assertEquals(joinAgentStatBo.getId(), agentId);
        assertEquals(joinAgentStatBo.getAgentStartTimestamp(), 1491274138454L);
        assertEquals(joinAgentStatBo.getTimestamp(), 1491274148454L);

        List<JoinCpuLoadBo> joinCpuLoadBoList = joinAgentStatBo.getJoinCpuLoadBoList();
        assertEquals(joinCpuLoadBoList.size(), 2);

        JoinCpuLoadBo joinCpuLoadBo = joinCpuLoadBoList.get(0);
        assertEquals(joinCpuLoadBo.getId(), agentId);
        assertEquals(joinCpuLoadBo.getTimestamp(), 1491274148454L);
        assertEquals(joinCpuLoadBo.getJvmCpuLoad(), 10, 0);
        assertEquals(joinCpuLoadBo.getMinJvmCpuLoad(), 10, 0);
        assertEquals(joinCpuLoadBo.getMaxJvmCpuLoad(), 10, 0);
        assertEquals(joinCpuLoadBo.getSystemCpuLoad(), 30, 0);
        assertEquals(joinCpuLoadBo.getMinSystemCpuLoad(), 30, 0);
        assertEquals(joinCpuLoadBo.getMaxSystemCpuLoad(), 30, 0);

        joinCpuLoadBo = joinCpuLoadBoList.get(1);
        assertEquals(joinCpuLoadBo.getId(), agentId);
        assertEquals(joinCpuLoadBo.getTimestamp(), 1491275148454L);
        assertEquals(joinCpuLoadBo.getJvmCpuLoad(), 20, 0);
        assertEquals(joinCpuLoadBo.getMinJvmCpuLoad(), 20, 0);
        assertEquals(joinCpuLoadBo.getMaxJvmCpuLoad(), 20, 0);
        assertEquals(joinCpuLoadBo.getSystemCpuLoad(), 50, 0);
        assertEquals(joinCpuLoadBo.getMinSystemCpuLoad(), 50, 0);
        assertEquals(joinCpuLoadBo.getMaxSystemCpuLoad(), 50, 0);
    }

    @Test
    public void map2Test() {
        final String agentId = "testAgent";
        final JoinAgentStatBoMapper joinAgentStatBoMapper = new JoinAgentStatBoMapper();

        final TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch();
        tFAgentStatBatch.setStartTimestamp(1491274138454L);
        tFAgentStatBatch.setAgentId(agentId);

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(agentId);
        tFAgentStat.setTimestamp(1491274148454L);

        final TFJvmGc tFJvmGc = new TFJvmGc();
        tFJvmGc.setJvmMemoryHeapUsed(1000);
        tFJvmGc.setJvmMemoryNonHeapUsed(300);
        tFAgentStat.setGc(tFJvmGc);

        final TFAgentStat tFAgentStat2 = new TFAgentStat();
        tFAgentStat2.setAgentId(agentId);
        tFAgentStat2.setTimestamp(1491275148454L);

        final TFJvmGc tFJvmGc2 = new TFJvmGc();
        tFJvmGc2.setJvmMemoryHeapUsed(2000);
        tFJvmGc2.setJvmMemoryNonHeapUsed(500);
        tFAgentStat2.setGc(tFJvmGc2);


        final List<TFAgentStat> tFAgentStatList = new ArrayList<>(2);
        tFAgentStatList.add(tFAgentStat);
        tFAgentStatList.add(tFAgentStat2);
        tFAgentStatBatch.setAgentStats(tFAgentStatList);

        JoinAgentStatBo joinAgentStatBo = joinAgentStatBoMapper.map(tFAgentStatBatch);
        assertEquals(joinAgentStatBo.getId(), agentId);
        assertEquals(joinAgentStatBo.getAgentStartTimestamp(), 1491274138454L);
        assertEquals(joinAgentStatBo.getTimestamp(), 1491274148454L);

        List<JoinMemoryBo> joinMemoryBoList = joinAgentStatBo.getJoinMemoryBoList();
        assertEquals(joinMemoryBoList.size(), 2);

        JoinMemoryBo joinMemoryBo = joinMemoryBoList.get(0);
        assertEquals(joinMemoryBo.getId(), agentId);
        assertEquals(joinMemoryBo.getTimestamp(), 1491274148454L);
        assertEquals(joinMemoryBo.getHeapUsed(), 1000);
        assertEquals(joinMemoryBo.getNonHeapUsed(), 300);

        JoinMemoryBo joinMemoryBo2 = joinMemoryBoList.get(1);
        assertEquals(joinMemoryBo2.getId(), agentId);
        assertEquals(joinMemoryBo2.getTimestamp(), 1491275148454L);
        assertEquals(joinMemoryBo2.getHeapUsed(), 2000);
        assertEquals(joinMemoryBo2.getNonHeapUsed(), 500);
    }

    @Test
    public void map3Test() {
        final String agentId = "testAgent";
        final JoinAgentStatBoMapper joinAgentStatBoMapper = new JoinAgentStatBoMapper();

        final TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch();
        tFAgentStatBatch.setStartTimestamp(1491274138454L);
        tFAgentStatBatch.setAgentId(agentId);

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(agentId);
        tFAgentStat.setTimestamp(1491274148454L);
        tFAgentStat.setCollectInterval(5000);

        final TFTransaction tFTransaction = new TFTransaction();
        tFTransaction.setSampledNewCount(10);
        tFTransaction.setSampledContinuationCount(20);
        tFTransaction.setUnsampledNewCount(40);
        tFTransaction.setUnsampledContinuationCount(50);
        tFAgentStat.setTransaction(tFTransaction);

        final TFAgentStat tFAgentStat2 = new TFAgentStat();
        tFAgentStat2.setAgentId(agentId);
        tFAgentStat2.setTimestamp(1491275148454L);
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

        JoinAgentStatBo joinAgentStatBo = joinAgentStatBoMapper.map(tFAgentStatBatch);
        assertEquals(joinAgentStatBo.getId(), agentId);
        assertEquals(joinAgentStatBo.getAgentStartTimestamp(), 1491274138454L);
        assertEquals(joinAgentStatBo.getTimestamp(), 1491274148454L);

        List<JoinTransactionBo> joinTransactionBoList = joinAgentStatBo.getJoinTransactionBoList();
        assertEquals(joinTransactionBoList.size(), 2);

        JoinTransactionBo joinTransactionBo = joinTransactionBoList.get(0);
        assertEquals(joinTransactionBo.getId(), agentId);
        assertEquals(joinTransactionBo.getTimestamp(), 1491274148454L);
        assertEquals(joinTransactionBo.getCollectInterval(), 5000);
        assertEquals(joinTransactionBo.getTotalCount(), 120);
        assertEquals(joinTransactionBo.getMaxTotalCount(), 120);
        assertEquals(joinTransactionBo.getMaxTotalCountAgentId(), agentId);
        assertEquals(joinTransactionBo.getMinTotalCount(), 120);
        assertEquals(joinTransactionBo.getMinTotalCountAgentId(), agentId);

        JoinTransactionBo joinTransactionBo2 = joinTransactionBoList.get(1);
        assertEquals(joinTransactionBo2.getId(), agentId);
        assertEquals(joinTransactionBo2.getTimestamp(), 1491275148454L);
        assertEquals(joinTransactionBo2.getCollectInterval(), 5000);
        assertEquals(joinTransactionBo2.getTotalCount(), 124);
        assertEquals(joinTransactionBo2.getMaxTotalCount(), 124);
        assertEquals(joinTransactionBo2.getMaxTotalCountAgentId(), agentId);
        assertEquals(joinTransactionBo2.getMinTotalCount(), 124);
        assertEquals(joinTransactionBo2.getMinTotalCountAgentId(), agentId);
    }

}