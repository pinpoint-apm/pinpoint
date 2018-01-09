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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.flink.mapper.thrift.stat.JoinCpuLoadBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFCpuLoad;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinCpuLoadBoMapperTest {
    @Test
    public void mapTest() throws Exception {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);

        final TFCpuLoad tfCpuLoad = new TFCpuLoad();
        tfCpuLoad.setJvmCpuLoad(10);
        tfCpuLoad.setSystemCpuLoad(30);
        tFAgentStat.setCpuLoad(tfCpuLoad);

        final JoinCpuLoadBoMapper mapper = new JoinCpuLoadBoMapper();
        final JoinCpuLoadBo joinCpuLoadBo = mapper.map(tFAgentStat);

        assertNotNull(joinCpuLoadBo);
        assertEquals(joinCpuLoadBo.getId(), "testAgent");
        assertEquals(joinCpuLoadBo.getTimestamp(), 1491274138454L);
        assertEquals(joinCpuLoadBo.getJvmCpuLoad(), 10, 0);
        assertEquals(joinCpuLoadBo.getMinJvmCpuLoad(), 10, 0);
        assertEquals(joinCpuLoadBo.getMaxJvmCpuLoad(), 10, 0);
        assertEquals(joinCpuLoadBo.getSystemCpuLoad(), 30, 0);
        assertEquals(joinCpuLoadBo.getMinSystemCpuLoad(), 30, 0);
        assertEquals(joinCpuLoadBo.getMaxSystemCpuLoad(), 30, 0);
    }

    @Test
    public void map2Test() {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);

        final JoinCpuLoadBoMapper mapper = new JoinCpuLoadBoMapper();
        final JoinCpuLoadBo joinCpuLoadBo = mapper.map(tFAgentStat);
        assertEquals(joinCpuLoadBo, joinCpuLoadBo.EMPTY_JOIN_CPU_LOAD_BO);
    }
}