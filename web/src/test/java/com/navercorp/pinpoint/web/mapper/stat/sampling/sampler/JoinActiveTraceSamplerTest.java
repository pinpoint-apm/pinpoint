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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinActiveTraceSamplerTest {

    @Test
    public void sampleDataPointsTest() {
        long currentTime = 1487149800000L;
        JoinActiveTraceSampler sampler = new JoinActiveTraceSampler();
        List<JoinActiveTraceBo> joinActiveTraceBoList = createJoinActiveTraceBoList(currentTime);
        AggreJoinActiveTraceBo aggreJoinActiveTraceBo = sampler.sampleDataPoints(1, currentTime, joinActiveTraceBoList, JoinActiveTraceBo.EMPTY_JOIN_ACTIVE_TRACE_BO);

        assertEquals(aggreJoinActiveTraceBo.getId(), "test_app");
        assertEquals(aggreJoinActiveTraceBo.getHistogramSchemaType(), 1);
        assertEquals(aggreJoinActiveTraceBo.getVersion(), 2);
        assertEquals(aggreJoinActiveTraceBo.getTotalCount(), 130);
        assertEquals(aggreJoinActiveTraceBo.getMinTotalCount(), 10);
        assertEquals(aggreJoinActiveTraceBo.getMinTotalCountAgentId(), "app_1_1");
        assertEquals(aggreJoinActiveTraceBo.getMaxTotalCount(), 560);
        assertEquals(aggreJoinActiveTraceBo.getMaxTotalCountAgentId(), "app_4_2");
        assertEquals(aggreJoinActiveTraceBo.getTimestamp(), 1487149800000L);
    }

    private List<JoinActiveTraceBo> createJoinActiveTraceBoList(long currentTime) {
        final String id = "test_app";
        List<JoinActiveTraceBo> joinActiveTraceBoList = new ArrayList<JoinActiveTraceBo>();
        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo(id, 1, (short)2, 150, 10, "app_1_1", 230, "app_1_2", currentTime);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo(id, 1, (short)2, 110, 22, "app_2_1", 330, "app_2_2", currentTime + 5000);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo(id, 1, (short)2, 120, 24, "app_3_1", 540, "app_3_2", currentTime + 10000);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo(id, 1, (short)2, 130, 25, "app_4_1", 560, "app_4_2", currentTime + 15000);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo(id, 1, (short)2, 140, 12, "app_5_1", 260, "app_5_2", currentTime + 20000);
        joinActiveTraceBoList.add(joinActiveTraceBo1);
        joinActiveTraceBoList.add(joinActiveTraceBo2);
        joinActiveTraceBoList.add(joinActiveTraceBo3);
        joinActiveTraceBoList.add(joinActiveTraceBo4);
        joinActiveTraceBoList.add(joinActiveTraceBo5);

        return joinActiveTraceBoList;
    }

}