/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.memory;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Taejin Koo
 */
public class MemoryAgentStatisticsDaoTest {

    private static List<AgentCountStatistics> testDataList;

    @BeforeAll
    public static void setup() {
        testDataList = createTestData(100);
    }

    private static List<AgentCountStatistics> createTestData(int size) {
        List<AgentCountStatistics> data = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            AgentCountStatistics agentCountStatistics = new AgentCountStatistics(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE), (i * 100) + 100);
            data.add(agentCountStatistics);
        }

        return data;
    }

    @Test
    public void simpleTest() {
        MemoryAgentStatisticsDao dao = new MemoryAgentStatisticsDao();
        for (AgentCountStatistics testData : testDataList) {
            dao.insertAgentCount(testData);
        }

        Range range = Range.between(660L, 1320L);
        List<AgentCountStatistics> agentCountStatisticses = dao.selectAgentCount(range);
        assertThat(agentCountStatisticses).hasSize(7);


        range = Range.between(7100L, System.currentTimeMillis());
        agentCountStatisticses = dao.selectAgentCount(range);
        assertThat(agentCountStatisticses).hasSize(30);

        range = Range.between(0L, System.currentTimeMillis());
        agentCountStatisticses = dao.selectAgentCount(range);
        assertThat(agentCountStatisticses).hasSize(100);

        long currentTime = System.currentTimeMillis();
        range = Range.between(currentTime, currentTime + 100);
        agentCountStatisticses = dao.selectAgentCount(range);
        assertThat(agentCountStatisticses).isEmpty();

        agentCountStatisticses = dao.selectLatestAgentCount(10);
        assertThat(agentCountStatisticses).hasSize(10);
    }

}
