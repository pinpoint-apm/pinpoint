/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Roy Kim
 */
public class JoinDirectBufferSamplerTest {
    @Test
    public void sampleDataPoints() throws Exception {
        JoinDirectBufferSampler joinDirectBufferSampler = new JoinDirectBufferSampler();
        List<JoinDirectBufferBo>joinDirectBufferBoList = new ArrayList<>(5);

        long timeStamp = new Date().getTime();
        joinDirectBufferBoList.add(new JoinDirectBufferBo("testApp", 11, 60, "agent1_1", 20, "agent1_2", 10, 60, "agent1_3", 47, "agent1_4", 11, 61, "agent1_5", 21, "agent1_6", 10, 60, "agent1_7", 46, "agent1_8", timeStamp + 5000));
        joinDirectBufferBoList.add(new JoinDirectBufferBo("testApp", 22, 52, "agent2_1", 10, "agent2_2", 20, 70, "agent2_3", 24, "agent2_4", 22, 53, "agent2_5", 11, "agent2_6", 20, 70, "agent2_7", 23, "agent2_8", timeStamp + 10000));
        joinDirectBufferBoList.add(new JoinDirectBufferBo("testApp", 33, 39, "agent3_1", 90, "agent3_2", 30, 85, "agent3_3", 33, "agent3_4", 33, 40, "agent3_5", 91, "agent3_6", 30, 85, "agent3_7", 32, "agent3_8", timeStamp + 15000));
        joinDirectBufferBoList.add(new JoinDirectBufferBo("testApp", 44, 42, "agent4_1", 25, "agent4_2", 40, 58, "agent4_3", 56, "agent4_4", 44, 43, "agent4_5", 26, "agent4_6", 40, 58, "agent4_7", 55, "agent4_8", timeStamp + 20000));
        joinDirectBufferBoList.add(new JoinDirectBufferBo("testApp", 55, 55, "agent5_1", 54, "agent5_2", 50, 86, "agent5_3", 76, "agent5_4", 55, 56, "agent5_5", 55, "agent5_6", 50, 86, "agent5_7", 75, "agent5_8", timeStamp + 25000));

        AggreJoinDirectBufferBo aggreJoinDirectBufferBo = joinDirectBufferSampler.sampleDataPoints(0, new Date().getTime(), joinDirectBufferBoList,  new JoinDirectBufferBo());
        assertEquals(aggreJoinDirectBufferBo.getId(), "testApp");
        assertEquals(aggreJoinDirectBufferBo.getAvgDirectCount(), 33, 0);
        assertEquals(aggreJoinDirectBufferBo.getMinDirectCount(), 10, 0);
        assertEquals(aggreJoinDirectBufferBo.getMinDirectCountAgentId(), "agent2_2");
        assertEquals(aggreJoinDirectBufferBo.getMaxDirectCount(), 60, 0);
        assertEquals(aggreJoinDirectBufferBo.getMaxDirectCountAgentId(), "agent1_1");

        assertEquals(aggreJoinDirectBufferBo.getAvgDirectMemoryUsed(), 30, 0);
        assertEquals(aggreJoinDirectBufferBo.getMinDirectMemoryUsed(), 24, 0);
        assertEquals(aggreJoinDirectBufferBo.getMinDirectMemoryUsedAgentId(), "agent2_4");
        assertEquals(aggreJoinDirectBufferBo.getMaxDirectMemoryUsed(), 86, 0);
        assertEquals(aggreJoinDirectBufferBo.getMaxDirectMemoryUsedAgentId(), "agent5_3");

        assertEquals(aggreJoinDirectBufferBo.getId(), "testApp");
        assertEquals(aggreJoinDirectBufferBo.getAvgMappedCount(), 33, 0);
        assertEquals(aggreJoinDirectBufferBo.getMinMappedCount(), 11, 0);
        assertEquals(aggreJoinDirectBufferBo.getMinMappedCountAgentId(), "agent2_6");
        assertEquals(aggreJoinDirectBufferBo.getMaxMappedCount(), 61, 0);
        assertEquals(aggreJoinDirectBufferBo.getMaxMappedCountAgentId(), "agent1_5");

        assertEquals(aggreJoinDirectBufferBo.getAvgMappedMemoryUsed(), 30, 0);
        assertEquals(aggreJoinDirectBufferBo.getMinMappedMemoryUsed(), 23, 0);
        assertEquals(aggreJoinDirectBufferBo.getMinMappedMemoryUsedAgentId(), "agent2_8");
        assertEquals(aggreJoinDirectBufferBo.getMaxMappedMemoryUsed(), 86, 0);
        assertEquals(aggreJoinDirectBufferBo.getMaxMappedMemoryUsedAgentId(), "agent5_7");
    }

}