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
