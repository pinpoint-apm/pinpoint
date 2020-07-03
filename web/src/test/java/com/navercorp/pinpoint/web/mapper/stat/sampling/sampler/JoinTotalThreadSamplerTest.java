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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTotalThreadCountBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JoinTotalThreadSamplerTest {
    @Test
    public void sampleDataPoints() {
        JoinTotalThreadCountSampler joinTotalThreadCountSampler = new JoinTotalThreadCountSampler();
        List<JoinTotalThreadCountBo> joinTotalThreadCountBoList = new ArrayList<>(5);

        long timeStamp = new Date().getTime();
        joinTotalThreadCountBoList.add(new JoinTotalThreadCountBo("testApp", timeStamp + 5000, 11, 60, "agent1_1", 20, "agent1_2"));
        joinTotalThreadCountBoList.add(new JoinTotalThreadCountBo("testApp", timeStamp + 10000, 22, 52, "agent2_1", 10, "agent2_2"));
        joinTotalThreadCountBoList.add(new JoinTotalThreadCountBo("testApp", timeStamp + 15000, 33, 39, "agent3_1", 90, "agent3_2"));
        joinTotalThreadCountBoList.add(new JoinTotalThreadCountBo("testApp", timeStamp + 20000, 44, 42, "agent4_1", 25, "agent4_2"));
        joinTotalThreadCountBoList.add(new JoinTotalThreadCountBo("testApp", timeStamp + 25000, 55, 55, "agent5_1", 54, "agent5_2"));

        AggreJoinTotalThreadCountBo aggreJoinTotalThraedCountBo = joinTotalThreadCountSampler.sampleDataPoints(0, new Date().getTime(), joinTotalThreadCountBoList,  new JoinTotalThreadCountBo());
        assertEquals(aggreJoinTotalThraedCountBo.getId(), "testApp");
        assertEquals(aggreJoinTotalThraedCountBo.getTotalThreadCountJoinValue(), new JoinLongFieldBo(33L, 39L, "agent3_1", 90L, "agent3_2"));
    }
}
