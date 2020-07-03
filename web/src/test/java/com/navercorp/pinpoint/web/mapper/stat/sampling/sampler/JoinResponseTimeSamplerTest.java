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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinResponseTimeSamplerTest {

    @Test
    public void sampleDataPointsTest() {
        long currentTime = 1487149800000L;
        JoinResponseTimeSampler joinResponseTimeSampler = new JoinResponseTimeSampler();
        List<JoinResponseTimeBo> joinResponseTimeBoList = createJoinResponseTimeList(currentTime);
        AggreJoinResponseTimeBo aggreJoinResponseTimeBo = joinResponseTimeSampler.sampleDataPoints(1, currentTime, joinResponseTimeBoList, JoinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO);
        assertEquals(aggreJoinResponseTimeBo.getId(), "test_app");
        assertEquals(aggreJoinResponseTimeBo.getTimestamp(), 1487149800000L);
        assertEquals(aggreJoinResponseTimeBo.getResponseTimeJoinValue(), new JoinLongFieldBo(3000L, 2L, "app_1_1", 9000L, "app_2_1"));
    }

    private List<JoinResponseTimeBo> createJoinResponseTimeList(long currentTime) {
        final String id = "test_app";
        List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo(id, currentTime, 3000, 2, "app_1_1", 6000, "app_1_1");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo(id, currentTime + 5000, 4000, 200, "app_2_1", 9000, "app_2_1");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo(id, currentTime + 10000, 2000, 20, "app_3_1", 7000, "app_3_1");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo(id, currentTime + 15000, 5000, 20, "app_4_1", 8000, "app_4_1");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo(id, currentTime + 20000, 1000, 10, "app_5_1", 6600, "app_5_1");
        joinResponseTimeBoList.add(joinResponseTimeBo1);
        joinResponseTimeBoList.add(joinResponseTimeBo2);
        joinResponseTimeBoList.add(joinResponseTimeBo3);
        joinResponseTimeBoList.add(joinResponseTimeBo4);
        joinResponseTimeBoList.add(joinResponseTimeBo5);
        return joinResponseTimeBoList;
    }

    @Test
    public void sampleDataPoints2Test() {
        long currentTime = 1487149800000L;
        JoinResponseTimeSampler joinResponseTimeSampler = new JoinResponseTimeSampler();
        List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
        AggreJoinResponseTimeBo aggreJoinResponseTimeBo = joinResponseTimeSampler.sampleDataPoints(1, currentTime, joinResponseTimeBoList, JoinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO);
        assertEquals(aggreJoinResponseTimeBo.getId(), JoinResponseTimeBo.UNKNOWN_ID);
        assertEquals(aggreJoinResponseTimeBo.getTimestamp(), 1487149800000L);
        assertEquals(aggreJoinResponseTimeBo.getResponseTimeJoinValue(), new JoinLongFieldBo(JoinResponseTimeBo.UNCOLLECTED_VALUE, JoinResponseTimeBo.UNCOLLECTED_VALUE, JoinResponseTimeBo.UNKNOWN_AGENT, JoinResponseTimeBo.UNCOLLECTED_VALUE, JoinResponseTimeBo.UNKNOWN_AGENT));
    }
}