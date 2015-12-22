/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.navercorp.pinpoint.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.collector.handler.AgentStatHandler;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;

/**
 * @author hyungil.jeong
 */
public class AgentStatHandlerTest {

    @Mock
    private AgentStatDao agentStatDao;

    @InjectMocks
    private AgentStatHandler agentStatHandler = new AgentStatHandler();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleForTAgentStat() {
        // Given
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp);
        // When
        agentStatHandler.handle(agentStat);
        // Then
        verify(agentStatDao).insert(any(TAgentStat.class));
    }

    @Test
    public void testHandleForTAgentStatBatch() {
        // Given
        final int numBatches = 6;
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStatBatch agentStatBatch = createAgentStatBatch(agentId, startTimestamp, numBatches);
        // When
        agentStatHandler.handle(agentStatBatch);
        // Then
        verify(agentStatDao, times(numBatches)).insert(any(TAgentStat.class));
    }

    @Test(expected=IllegalArgumentException.class)
    public void handleShouldThrowIllegalArgumentExceptionForIncorrectTBaseObjects() {
        // Given
        final TAgentInfo wrongTBaseObject = new TAgentInfo();
        // When
        agentStatHandler.handle(wrongTBaseObject);
        // Then
        fail();
    }

    private TAgentStatBatch createAgentStatBatch(String agentId, long startTimestamp, int numBatches) {
        final TAgentStatBatch agentStatBatch = new TAgentStatBatch();
        agentStatBatch.setAgentId(agentId);
        agentStatBatch.setStartTimestamp(startTimestamp);
        final List<TAgentStat> agentStats = new ArrayList<>(numBatches);
        for (int i = 0; i < numBatches; ++i) {
            agentStats.add(createAgentStat(agentId, startTimestamp));
        }
        agentStatBatch.setAgentStats(agentStats);
        return agentStatBatch;
    }

    private TAgentStat createAgentStat(String agentId, long startTimestamp) {
        final TAgentStat agentStat = new TAgentStat();
        agentStat.setAgentId(agentId);
        agentStat.setStartTimestamp(startTimestamp);
        agentStat.setGc(new TJvmGc());
        agentStat.setCpuLoad(new TCpuLoad());
        return agentStat;
    }

}
