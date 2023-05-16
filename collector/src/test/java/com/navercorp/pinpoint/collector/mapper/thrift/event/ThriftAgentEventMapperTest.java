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
import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ThriftAgentEventMapperTest {

    @Mock
    private ThriftDeadlockEventBoMapper deadlockEventBoMapper;

    @Mock
    private ThriftDeadlockBoMapper deadlockBoMapper;

    @InjectMocks
    private ThriftAgentEventMapper agentEventMapper;

    @Test
    public void simpleTest1() {
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final long eventTimestamp = startTimestamp;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp, eventTimestamp, 2);

        DeadlockBo deadlockBo = new DeadlockBo(agentStat.getDeadlock().getDeadlockedThreadCount(), List.of());
        DeadlockEventBo expectedEventBo = new DeadlockEventBo(agentId, startTimestamp, eventTimestamp, AgentEventType.AGENT_DEADLOCK_DETECTED, deadlockBo);

        when(this.deadlockEventBoMapper.map(agentId, startTimestamp, startTimestamp, agentStat.getDeadlock())).thenReturn(expectedEventBo);
        AgentEventBo actualEventBo = agentEventMapper.map(agentStat);
        Assertions.assertEquals(expectedEventBo, actualEventBo);
    }

    @Test
    public void simpleTest2() {
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final long eventTimestamp = startTimestamp;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp, eventTimestamp, 0);

        DeadlockBo deadlockBo = new DeadlockBo();
        DeadlockEventBo expectedEventBo = new DeadlockEventBo(agentId, startTimestamp, eventTimestamp, AgentEventType.AGENT_DEADLOCK_DETECTED, deadlockBo);

        when(this.deadlockEventBoMapper.map(any(String.class), any(Long.class), any(Long.class), any(TDeadlock.class))).thenReturn(expectedEventBo);

        AgentEventBo actualEventBo = agentEventMapper.map(agentStat);
        Assertions.assertNull(actualEventBo);
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
