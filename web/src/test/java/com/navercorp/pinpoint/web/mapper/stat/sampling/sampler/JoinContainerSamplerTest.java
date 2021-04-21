/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinContainerBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinContainerBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Hyunjoon Cho
 */
public class JoinContainerSamplerTest {
    @Test
    public void sampleDataPoints() throws Exception {
        JoinContainerSampler joinContainerSampler = new JoinContainerSampler();
        List<JoinContainerBo> joinContainerBoList = new ArrayList<>(5);

        long timeStamp = new Date().getTime();
        joinContainerBoList.add(new JoinContainerBo("testApp", 0.11, 0.60, "agent1_1", 0.20, "agent1_2", 0.10, 0.60, "agent1_3", 0.47, "agent1_4", 11, 61, "agent1_5", 21, "agent1_6", 11, 61, "agent1_7", 21, "agent1_8", timeStamp + 5000));
        joinContainerBoList.add(new JoinContainerBo("testApp", 0.22, 0.52, "agent2_1", 0.10, "agent2_2", 0.20, 0.70, "agent2_3", 0.24, "agent2_4", 22, 53, "agent2_5", 11, "agent2_6", 22, 53, "agent2_7", 11, "agent2_8", timeStamp + 10000));
        joinContainerBoList.add(new JoinContainerBo("testApp", 0.33, 0.39, "agent3_1", 0.90, "agent3_2", 0.30, 0.85, "agent3_3", 0.33, "agent3_4", 33, 40, "agent3_5", 91, "agent3_6", 33, 40, "agent3_7", 91, "agent3_8", timeStamp + 15000));
        joinContainerBoList.add(new JoinContainerBo("testApp", 0.44, 0.42, "agent4_1", 0.25, "agent4_2", 0.40, 0.58, "agent4_3", 0.56, "agent4_4", 44, 43, "agent4_5", 26, "agent4_6", 44, 43, "agent4_7", 26, "agent4_8", timeStamp + 20000));
        joinContainerBoList.add(new JoinContainerBo("testApp", 0.55, 0.55, "agent5_1", 0.54, "agent5_2", 0.50, 0.86, "agent5_3", 0.76, "agent5_4", 55, 56, "agent5_5", 55, "agent5_6", 55, 56, "agent5_7", 55, "agent5_8", timeStamp + 25000));

        AggreJoinContainerBo aggreJoinContainerBo = joinContainerSampler.sampleDataPoints(0, new Date().getTime(), joinContainerBoList,  new JoinContainerBo());
        assertEquals(aggreJoinContainerBo.getId(), "testApp");
        assertEquals(aggreJoinContainerBo.getUserCpuUsageJoinValue(), new JoinDoubleFieldBo(0.33, 0.10, "agent2_2", 0.60, "agent1_1"));
        assertEquals(aggreJoinContainerBo.getSystemCpuUsageJoinValue(), new JoinDoubleFieldBo(0.30, 0.24, "agent2_4", 0.86, "agent5_3"));
        assertEquals(aggreJoinContainerBo.getMemoryMaxJoinValue(), new JoinLongFieldBo(33L, 11L, "agent2_6", 61L, "agent1_5"));
        assertEquals(aggreJoinContainerBo.getMemoryUsageJoinValue(), new JoinLongFieldBo(33L, 11L, "agent2_8", 61L, "agent1_7"));
    }
}
