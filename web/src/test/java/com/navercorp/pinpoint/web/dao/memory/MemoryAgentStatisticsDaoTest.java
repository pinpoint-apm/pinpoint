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

import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Taejin Koo
 */
public class MemoryAgentStatisticsDaoTest {

    private static List<AgentCountStatistics> testDataList;

    @BeforeClass
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
    public void simpleTest() throws Exception {
        MemoryAgentStatisticsDao dao = new MemoryAgentStatisticsDao();
        for (AgentCountStatistics testData : testDataList) {
            dao.insertAgentCount(testData);
        }

        Range range = new Range(660L, 1320L);
        List<AgentCountStatistics> agentCountStatisticses = dao.selectAgentCount(range);
        Assert.assertEquals(7, agentCountStatisticses.size());


        range = new Range(7100L, System.currentTimeMillis());
        agentCountStatisticses = dao.selectAgentCount(range);
        Assert.assertEquals(30, agentCountStatisticses.size());

        range = new Range(0L, System.currentTimeMillis());
        agentCountStatisticses = dao.selectAgentCount(range);
        Assert.assertEquals(100, agentCountStatisticses.size());

        long currentTime = System.currentTimeMillis();
        range = new Range(currentTime, currentTime + 100);
        agentCountStatisticses = dao.selectAgentCount(range);
        Assert.assertEquals(0, agentCountStatisticses.size());
    }

}
