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

package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineSegment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@ExtendWith(MockitoExtension.class)
public class AgentWarningStatServiceTest {

    private static final long CURRENT_TIME = System.currentTimeMillis();
    private static final long START_TIME = CURRENT_TIME - TimeUnit.DAYS.toMillis(1);
    private static final long TIME = 60000 * 5;

    @Mock
    private AgentStatDao<DeadlockThreadCountBo> deadlockDao;

    private AgentWarningStatService agentWarningStatService;

    @BeforeEach
    public void setUp() throws Exception {
        this.agentWarningStatService = new AgentWarningStatServiceImpl(deadlockDao);
    }

    @Test
    public void selectTest1() {
        Range range = Range.between(CURRENT_TIME - TIME, CURRENT_TIME);

        List<DeadlockThreadCountBo> mockData = createMockData(10, 5000);
        when(deadlockDao.getAgentStatList("pinpoint", range)).thenReturn(mockData);
        List<AgentStatusTimelineSegment> timelineSegmentList = agentWarningStatService.select("pinpoint", range);
        Assertions.assertEquals(1, timelineSegmentList.size());
    }

    @Test
    public void selectTest2() {
        Range range = Range.between(CURRENT_TIME - TIME, CURRENT_TIME);

        List<DeadlockThreadCountBo> mockData = createMockData(10, 70000);
        when(deadlockDao.getAgentStatList("pinpoint", range)).thenReturn(mockData);
        List<AgentStatusTimelineSegment> timelineSegmentList = agentWarningStatService.select("pinpoint", range);
        Assertions.assertEquals(10, timelineSegmentList.size());
    }

    private List<DeadlockThreadCountBo> createMockData(int mockSize, long interval) {
        long timestamp = ThreadLocalRandom.current().nextLong(START_TIME, CURRENT_TIME);

        List<DeadlockThreadCountBo> deadlockThreadCountBoList = new ArrayList<>(mockSize);
        for (int i = 0; i < mockSize; i++) {
            DeadlockThreadCountBo deadlockThreadCountBo = new DeadlockThreadCountBo();
            deadlockThreadCountBo.setAgentId("pinpoint");
            deadlockThreadCountBo.setStartTimestamp(START_TIME);
            deadlockThreadCountBo.setTimestamp(timestamp + (i * interval));

            deadlockThreadCountBoList.add(deadlockThreadCountBo);
        }

        return deadlockThreadCountBoList;
    }

}
