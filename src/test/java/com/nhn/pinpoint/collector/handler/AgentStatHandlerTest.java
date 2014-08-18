package com.nhn.pinpoint.collector.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nhn.pinpoint.collector.dao.AgentStatDao;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TAgentStatBatch;
import com.nhn.pinpoint.thrift.dto.TCpuLoad;
import com.nhn.pinpoint.thrift.dto.TJvmGc;

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
        agentStatHandler.handle(agentStat, new byte[0], 0, 0);
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
        agentStatHandler.handle(agentStatBatch, new byte[0], 0, 0);
        // Then
        verify(agentStatDao, times(numBatches)).insert(any(TAgentStat.class));
    }

    @Test(expected=IllegalArgumentException.class)
    public void handleShouldThrowIllegalArgumentExceptionForIncorrectTBaseObjects() {
        // Given
        final TAgentInfo wrongTBaseObject = new TAgentInfo();
        // When
        agentStatHandler.handle(wrongTBaseObject, new byte[0], 0, 0);
        // Then
        fail();
    }

    private TAgentStatBatch createAgentStatBatch(String agentId, long startTimestamp, int numBatches) {
        final TAgentStatBatch agentStatBatch = new TAgentStatBatch();
        agentStatBatch.setAgentId(agentId);
        agentStatBatch.setStartTimestamp(startTimestamp);
        final List<TAgentStat> agentStats = new ArrayList<TAgentStat>(numBatches);
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
