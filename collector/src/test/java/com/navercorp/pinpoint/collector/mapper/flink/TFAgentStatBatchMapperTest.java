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

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class TFAgentStatBatchMapperTest {
    public static final String TEST_AGENT = "test_agent";
    public static final long startTimestamp = 1496370596375L;
    public static final long collectTime1st = startTimestamp + 5000;
    public static final long collectTime2nd = collectTime1st + 5000;
    public static final long collectTime3rd = collectTime2nd + 5000;

    @Test
    public void mapTest() throws Exception {
        AgentStatBo agentStatBo = createCpuLoadBoList();

        TFAgentStatMapper tFAgentStatMapper = new TFAgentStatMapper(new TFCpuLoadMapper[]{new TFCpuLoadMapper()});
        TFAgentStatBatchMapper mapper = new TFAgentStatBatchMapper(tFAgentStatMapper);
        TFAgentStatBatch tFAgentStatBatch = mapper.map(agentStatBo);

        assertEquals(TEST_AGENT, tFAgentStatBatch.getAgentId());
        assertEquals(startTimestamp, tFAgentStatBatch.getStartTimestamp());

        List<TFAgentStat> agentStatList = tFAgentStatBatch.getAgentStats();
        assertEquals(agentStatList.size(), 3);
    }

    private AgentStatBo createCpuLoadBoList() {
        AgentStatBo.Builder builder = AgentStatBo.newBuilder(TEST_AGENT, startTimestamp);

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
        cpuLoadBo2.setAgentId(TEST_AGENT);
        cpuLoadBo2.setTimestamp(collectTime2nd);
        cpuLoadBo2.setStartTimestamp(startTimestamp);
        cpuLoadBo2.setJvmCpuLoad(5);
        cpuLoadBo2.setSystemCpuLoad(6);
        AgentStatBo.Builder.StatBuilder statBuilder2 = builder.newStatBuilder(collectTime2nd);
        statBuilder2.addCpuLoad(cpuLoadBo2);

        return builder.build();
    }

}