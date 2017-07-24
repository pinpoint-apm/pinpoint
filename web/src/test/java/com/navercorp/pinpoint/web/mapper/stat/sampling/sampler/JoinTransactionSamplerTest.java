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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinTransactionSamplerTest {

    @Test
    public void sampleDataPointsTest() {
        long currentTime = 1487149800000L;
        JoinTransactionSampler joinTransactionSampler = new JoinTransactionSampler();
        List<JoinTransactionBo> joinTransactionBoList = createJoinTransactionBoList(currentTime);
        AggreJoinTransactionBo aggreJoinTransactionBo = joinTransactionSampler.sampleDataPoints(1, currentTime, joinTransactionBoList, JoinTransactionBo.EMPTY_TRANSACTION_BO);

        assertEquals(aggreJoinTransactionBo.getId(), "test_app");
        assertEquals(aggreJoinTransactionBo.getCollectInterval(),5000);
        assertEquals(aggreJoinTransactionBo.getTotalCount(), 130);
        assertEquals(aggreJoinTransactionBo.getMinTotalCount(), 10);
        assertEquals(aggreJoinTransactionBo.getMinTotalCountAgentId(), "app_1_1");
        assertEquals(aggreJoinTransactionBo.getMaxTotalCount(), 560);
        assertEquals(aggreJoinTransactionBo.getMaxTotalCountAgentId(), "app_4_2");
        assertEquals(aggreJoinTransactionBo.getTimestamp(), 1487149800000L);
    }

    private List<JoinTransactionBo> createJoinTransactionBoList(long currentTime) {
        final String id = "test_app";
        List<JoinTransactionBo> joinTransactionBoList = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo(id, 5000, 150, 10, "app_1_1", 230, "app_1_2", currentTime);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo(id, 5000, 110, 22, "app_2_1", 330, "app_2_2", currentTime + 5000);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo(id, 5000, 120, 24, "app_3_1", 540, "app_3_2", currentTime + 10000);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo(id, 5000, 130, 25, "app_4_1", 560, "app_4_2", currentTime + 15000);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo(id, 5000, 140, 12, "app_5_1", 260, "app_5_2", currentTime + 20000);
        joinTransactionBoList.add(joinTransactionBo1);
        joinTransactionBoList.add(joinTransactionBo2);
        joinTransactionBoList.add(joinTransactionBo3);
        joinTransactionBoList.add(joinTransactionBo4);
        joinTransactionBoList.add(joinTransactionBo5);
        return joinTransactionBoList;
    }

}