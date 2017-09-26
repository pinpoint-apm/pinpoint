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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFResponseTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinResponseTimeBoMapperTest {

    @Test
    public void mapTest() {
        final String agentId = "agentId";
        final TFResponseTime tFResponseTime = new TFResponseTime();
        tFResponseTime.setAvg(100);
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(agentId);
        tFAgentStat.setTimestamp(1491274148454L);
        tFAgentStat.setResponseTime(tFResponseTime);

        JoinResponseTimeBoMapper mapper = new JoinResponseTimeBoMapper();
        JoinResponseTimeBo joinResponseTimeBo = mapper.map(tFAgentStat);

        assertEquals(joinResponseTimeBo.getId(), agentId);
        assertEquals(joinResponseTimeBo.getTimestamp(), 1491274148454L);
        assertEquals(joinResponseTimeBo.getAvg(), 100);
        assertEquals(joinResponseTimeBo.getMinAvg(), 100);
        assertEquals(joinResponseTimeBo.getMinAvgAgentId(), agentId);
        assertEquals(joinResponseTimeBo.getMaxAvg(), 100);
        assertEquals(joinResponseTimeBo.getMaxAvgAgentId(), agentId);
    }

    @Test
    public void map2Test() {
        final String agentId = "agentId";
        final TFResponseTime tFResponseTime = new TFResponseTime();
        tFResponseTime.setAvg(100);
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId(agentId);
        tFAgentStat.setTimestamp(1491274148454L);

        JoinResponseTimeBoMapper mapper = new JoinResponseTimeBoMapper();
        JoinResponseTimeBo joinResponseTimeBo = mapper.map(tFAgentStat);

        assertEquals(joinResponseTimeBo, JoinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO);
    }

}