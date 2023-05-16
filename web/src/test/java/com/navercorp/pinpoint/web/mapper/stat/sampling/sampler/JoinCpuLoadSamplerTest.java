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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinCpuLoadSamplerTest {
    @Test
    public void sampleDataPoints() throws Exception {
        JoinCpuLoadSampler joinCpuLoadSampler = new JoinCpuLoadSampler();

        long timeStamp = System.currentTimeMillis();
        List<JoinCpuLoadBo> joinCpuLoadBoList = List.of(
                new JoinCpuLoadBo("testApp", 0.11, 0.60, "agent1_1", 0.20, "agent1_2", 0.1, 0.60, "agent1_3", 0.47, "agent1_4", timeStamp + 5000),
                new JoinCpuLoadBo("testApp", 0.22, 0.52, "agent2_1", 0.10, "agent2_2", 0.2, 0.70, "agent2_3", 0.24, "agent2_4", timeStamp + 10000),
                new JoinCpuLoadBo("testApp", 0.33, 0.39, "agent3_1", 0.9, "agent3_2", 0.3, 0.85, "agent3_3", 0.33, "agent3_4", timeStamp + 15000),
                new JoinCpuLoadBo("testApp", 0.44, 0.42, "agent4_1", 0.25, "agent4_2", 0.4, 0.58, "agent4_3", 0.56, "agent4_4", timeStamp + 20000),
                new JoinCpuLoadBo("testApp", 0.55, 0.55, "agent5_1", 0.54, "agent5_2", 0.5, 0.86, "agent5_3", 0.76, "agent5_4", timeStamp + 25000)
        );

        AggreJoinCpuLoadBo aggreJoinCpuLoadBo = joinCpuLoadSampler.sampleDataPoints(0, new Date().getTime(), joinCpuLoadBoList, new JoinCpuLoadBo());
        assertEquals(aggreJoinCpuLoadBo.getId(), "testApp");
        assertEquals(aggreJoinCpuLoadBo.getJvmCpuLoadJoinValue(), new JoinDoubleFieldBo(33.0, 10.0, "agent2_2", 60.0, "agent1_1"));
        assertEquals(aggreJoinCpuLoadBo.getSystemCpuLoadJoinValue(), new JoinDoubleFieldBo(30.0, (double) 24, "agent2_4", 86.0, "agent5_3"));
    }

}