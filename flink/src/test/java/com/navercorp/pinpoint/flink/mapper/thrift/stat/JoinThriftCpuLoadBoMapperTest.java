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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFCpuLoad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author minwoo.jung
 */
public class JoinThriftCpuLoadBoMapperTest {
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
        final JoinDoubleFieldBo jvmCpuLoadJoinValue = joinCpuLoadBo.getJvmCpuLoadJoinValue();
        assertEquals(jvmCpuLoadJoinValue.getAvg(), 10, 0);
        assertEquals(jvmCpuLoadJoinValue.getMin(), 10, 0);
        assertEquals(jvmCpuLoadJoinValue.getMax(), 10, 0);
        final JoinDoubleFieldBo systemCpuLoadJoinValue = joinCpuLoadBo.getSystemCpuLoadJoinValue();
        assertEquals(systemCpuLoadJoinValue.getAvg(), 30, 0);
        assertEquals(systemCpuLoadJoinValue.getMin(), 30, 0);
        assertEquals(systemCpuLoadJoinValue.getMax(), 30, 0);
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