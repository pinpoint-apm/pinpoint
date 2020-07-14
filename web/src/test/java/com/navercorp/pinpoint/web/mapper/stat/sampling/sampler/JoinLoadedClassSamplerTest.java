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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JoinLoadedClassSamplerTest {
    @Test
    public void sampleDataPoints() {
        JoinLoadedClassSampler joinLoadedClassSampler = new JoinLoadedClassSampler();
        List<JoinLoadedClassBo> joinLoadedClassBoList = new ArrayList<>(5);

        long timeStamp = new Date().getTime();
        joinLoadedClassBoList.add(new JoinLoadedClassBo("testApp", 11, 60, "agent1_1", 20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", timeStamp + 5000));
        joinLoadedClassBoList.add(new JoinLoadedClassBo("testApp", 22, 52, "agent2_1", 10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2",timeStamp + 10000));
        joinLoadedClassBoList.add(new JoinLoadedClassBo("testApp", 33, 39, "agent3_1", 90, "agent3_2", 33, 39, "agent3_1", 90, "agent3_2", timeStamp + 15000));
        joinLoadedClassBoList.add(new JoinLoadedClassBo("testApp", 44, 42, "agent4_1", 25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2", timeStamp + 20000));
        joinLoadedClassBoList.add(new JoinLoadedClassBo("testApp", 55, 55, "agent5_1", 54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2", timeStamp + 25000));

        AggreJoinLoadedClassBo aggreJoinLoadedClassBo = joinLoadedClassSampler.sampleDataPoints(0, new Date().getTime(), joinLoadedClassBoList,  new JoinLoadedClassBo());
        assertEquals(aggreJoinLoadedClassBo.getId(), "testApp");
        assertEquals(aggreJoinLoadedClassBo.getLoadedClassJoinValue(), new JoinLongFieldBo(33L, 10L, "agent2_2", 60L, "agent1_1"));
        assertEquals(aggreJoinLoadedClassBo.getUnloadedClassJoinValue(), new JoinLongFieldBo(33L, 10L, "agent2_2", 60L, "agent1_1"));
    }
}
