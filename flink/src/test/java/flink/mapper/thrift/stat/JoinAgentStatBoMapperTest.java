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

package flink.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.flink.mapper.thrift.stat.JoinAgentStatBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.flink.TFCpuLoad;
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

}