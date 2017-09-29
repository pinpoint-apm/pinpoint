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

package com.navercorp.pinpoint.collector.mapper.thrift.event;

import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockEventBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AgentEventMapperTest {

    @Mock
    private DeadlockEventBoMapper deadlockEventBoMapper;

    @InjectMocks
    private AgentEventMapper agentEventMapper;

    @Test
    public void simpleTest1() {
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final long eventTimestamp = startTimestamp;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp, eventTimestamp, 2);

        DeadlockEventBo expectedEventBo = getExpectedDeadlockEventBo(agentId, startTimestamp, eventTimestamp, agentStat);

        when(this.deadlockEventBoMapper.map(agentId, startTimestamp, startTimestamp, agentStat.getDeadlock())).thenReturn(expectedEventBo);

        AgentEventBo actualEventBo = agentEventMapper.map(agentStat);
        Assert.assertEquals(expectedEventBo, actualEventBo);
    }

    @Test
    public void simpleTest2() {
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final long eventTimestamp = startTimestamp;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp, eventTimestamp, 0);

        DeadlockEventBo expectedEventBo = getExpectedDeadlockEventBo(agentId, startTimestamp, eventTimestamp, agentStat);
        when(this.deadlockEventBoMapper.map(any(String.class), any(Long.class), any(Long.class), any(TDeadlock.class))).thenReturn(expectedEventBo);

        AgentEventBo actualEventBo = agentEventMapper.map(agentStat);
        Assert.assertNull(actualEventBo);
    }

    private DeadlockEventBo getExpectedDeadlockEventBo(String agentId, long startTimestamp, long eventTimestamp, TAgentStat agentStat) {
        DeadlockEventBoMapper mapper = new DeadlockEventBoMapper();
        return mapper.map(agentId, startTimestamp, startTimestamp, agentStat.getDeadlock());
    }

    private TAgentStat createAgentStat(String agentId, long startTimestamp, long eventTimestamp, int deadlockThreadCount) {
        final TAgentStat agentStat = new TAgentStat();
        agentStat.setAgentId(agentId);
        agentStat.setStartTimestamp(startTimestamp);
        agentStat.setTimestamp(eventTimestamp);

        if (deadlockThreadCount > 0) {
            TDeadlock tDeadlock = new TDeadlock();
            tDeadlock.setDeadlockedThreadCount(deadlockThreadCount);

            for (int i = 0; i < deadlockThreadCount; i++) {
                tDeadlock.addToDeadlockedThreadList(new TThreadDump());
            }

            agentStat.setDeadlock(tDeadlock);
        }

        return agentStat;
    }

}
