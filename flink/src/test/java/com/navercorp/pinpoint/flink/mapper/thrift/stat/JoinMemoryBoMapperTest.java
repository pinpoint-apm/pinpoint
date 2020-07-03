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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFJvmGc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinMemoryBoMapperTest {

    public static final String TEST_AGENT = "testAgent";

    @Test
    public void map1Test() throws Exception {
        final JoinMemoryBoMapper joinMemoryBoMapper = new JoinMemoryBoMapper();

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(TEST_AGENT);
        tFAgentStat.setTimestamp(1491274138454L);

        final TFJvmGc tFJvmGc = new TFJvmGc();
        tFJvmGc.setJvmMemoryHeapUsed(1000);
        tFJvmGc.setJvmMemoryNonHeapUsed(300);
        tFAgentStat.setGc(tFJvmGc);

        JoinMemoryBo joinMemoryBo = joinMemoryBoMapper.map(tFAgentStat);
        assertEquals(joinMemoryBo.getTimestamp(), 1491274138454L);
        assertEquals(joinMemoryBo.getId(), TEST_AGENT);
        assertEquals(joinMemoryBo.getHeapUsedJoinValue(), new JoinLongFieldBo(1000L, 1000L, TEST_AGENT, 1000L, TEST_AGENT));
        assertEquals(joinMemoryBo.getNonHeapUsedJoinValue(), new JoinLongFieldBo(300L, 300L, TEST_AGENT, 300L, TEST_AGENT));
    }

    @Test
    public void map2Test() {
        final JoinMemoryBoMapper joinMemoryBoMapper = new JoinMemoryBoMapper();

        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(TEST_AGENT);
        tFAgentStat.setTimestamp(1491274138454L);

        JoinMemoryBo joinMemoryBo = joinMemoryBoMapper.map(tFAgentStat);
        assertEquals(joinMemoryBo, joinMemoryBo.EMPTY_JOIN_MEMORY_BO);
    }

}