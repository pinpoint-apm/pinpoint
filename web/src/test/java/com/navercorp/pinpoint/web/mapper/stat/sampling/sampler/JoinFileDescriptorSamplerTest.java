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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinFileDescriptorBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Roy Kim
 */
public class JoinFileDescriptorSamplerTest {
    @Test
    public void sampleDataPoints() throws Exception {
        JoinFileDescriptorSampler joinFileDescriptorSampler = new JoinFileDescriptorSampler();
        List<JoinFileDescriptorBo>joinFileDescriptorBoList = new ArrayList<>(5);

        long timeStamp = new Date().getTime();
        joinFileDescriptorBoList.add(new JoinFileDescriptorBo("testApp", 11, 60, "agent1_1", 20, "agent1_2", timeStamp + 5000));
        joinFileDescriptorBoList.add(new JoinFileDescriptorBo("testApp", 22, 52, "agent2_1", 10, "agent2_2", timeStamp + 10000));
        joinFileDescriptorBoList.add(new JoinFileDescriptorBo("testApp", 33, 39, "agent3_1", 90, "agent3_2", timeStamp + 15000));
        joinFileDescriptorBoList.add(new JoinFileDescriptorBo("testApp", 44, 42, "agent4_1", 25, "agent4_2", timeStamp + 20000));
        joinFileDescriptorBoList.add(new JoinFileDescriptorBo("testApp", 55, 55, "agent5_1", 54, "agent5_2", timeStamp + 25000));

        AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo = joinFileDescriptorSampler.sampleDataPoints(0, new Date().getTime(), joinFileDescriptorBoList,  new JoinFileDescriptorBo());
        assertEquals(aggreJoinFileDescriptorBo.getId(), "testApp");
        assertEquals(aggreJoinFileDescriptorBo.getAvgOpenFDCount(), 33, 0);
        assertEquals(aggreJoinFileDescriptorBo.getMinOpenFDCount(), 10, 0);
        assertEquals(aggreJoinFileDescriptorBo.getMinOpenFDCountAgentId(), "agent2_2");
        assertEquals(aggreJoinFileDescriptorBo.getMaxOpenFDCount(), 60, 0);
        assertEquals(aggreJoinFileDescriptorBo.getMaxOpenFDCountAgentId(), "agent1_1");
    }

}